package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol }
import hmda.model.fi.{ Submission, SubmissionId, SubmissionStatus }
import hmda.model.validation.{ Macro, Quality, ValidationErrorType }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.{ HmdaFileValidator, SubmissionManager }
import hmda.persistence.processing.HmdaFileValidator._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.matching.Regex
import scala.util.{ Failure, Success }

trait SubmissionEditPaths
    extends InstitutionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with RequestVerificationUtils
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits
  def submissionEditsPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits") { (period, seqNr) =>
      timedGet { uri =>
        completeVerified(supervisor, querySupervisor, institutionId, period, seqNr, uri) {
          val fState = getStatusAndValidationState(supervisor, SubmissionId(institutionId, period, seqNr))
          onComplete(fState) {
            case Success((vs, status)) =>
              val s = EditCollection(editInfos(vs.syntacticalErrors))
              val v = EditCollection(editInfos(vs.validityErrors))
              val q = VerifiableEditCollection(vs.qualityVerified, editInfos(vs.qualityErrors))
              val m = VerifiableEditCollection(vs.macroVerified, editInfos(vs.larMacro))
              complete(ToResponseMarshallable(SummaryEditResults(s, v, q, m, status)))
            case Failure(error) => completeWithInternalError(uri, error)
          }
        }
      }
    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/csv
  def submissionEditCsvPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / "csv") { (period, seqNr) =>
      timedGet { uri =>
        completeVerified(supervisor, querySupervisor, institutionId, period, seqNr, uri) {
          val fValidationState = getValidationState(supervisor, institutionId, period, seqNr)
          onComplete(fValidationState) {
            case Success(validationState) =>
              val csv: String = validationErrorsToCsvResults(validationState)
              complete(ToResponseMarshallable(csv))
            case Failure(error) => completeWithInternalError(uri, error)
          }
        }
      }
    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/<editType>
  private val svqmRegex = new Regex("syntactical|validity|quality|macro")
  def submissionSingleEditPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / svqmRegex) { (period, seqNr, editType) =>
      timedGet { uri =>
        completeVerified(supervisor, querySupervisor, institutionId, period, seqNr, uri) {
          val fState = getStatusAndValidationState(supervisor, SubmissionId(institutionId, period, seqNr))
          onComplete(fState) {
            case Success((vs, status)) =>
              val edits = editInfos(editsOfType(editType, vs))
              complete(ToResponseMarshallable(SingleTypeEditResults(edits, status)))
            case Failure(error) => completeWithInternalError(uri, error)
          }
        }
      }
    }

  // /institutions/<institution>/filings/<period>/submissions/<submissionId>/edits/<edit>
  private val editNameRegex: Regex = new Regex("""[SVQ]\d\d\d""")

  def editFailureDetailsPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / editNameRegex) { (period, seqNr, editName) =>
      timedGet { uri =>
        completeVerified(supervisor, querySupervisor: ActorRef, institutionId, period, seqNr, uri) {
          parameters('page.as[Int] ? 1) { (page: Int) =>
            val fValidator: Future[ActorRef] = fHmdaFileValidator(supervisor, SubmissionId(institutionId, period, seqNr))
            val fPaginatedErrors: Future[(ActorRef, PaginatedErrors)] = for {
              va <- fValidator
              p <- (va ? GetNamedErrorResultsPaginated(editName, page)).mapTo[PaginatedErrors]
            } yield (va, p)

            onComplete(fPaginatedErrors) {
              case Success((hfvRef, errorCollection)) =>
                val rows: Seq[Future[EditResultRow]] = errorCollection.errors.map(validationErrorToResultRow(_, hfvRef))
                onComplete(Future.sequence(rows)) {
                  case Success(r) =>
                    val result = EditResult(editName, r, uri.path.toString, page, errorCollection.totalErrors)
                    complete(ToResponseMarshallable(result))
                  case Failure(e) => completeWithInternalError(uri, e)
                }
              case Failure(error) => completeWithInternalError(uri, error)
            }
          }
        }
      }
    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/quality|macro
  private val editTypeRegex = new Regex("quality|macro")
  def verifyEditsPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / editTypeRegex) { (period, seqNr, verificationType) =>
      timedPost { uri =>
        entity(as[EditsVerification]) { verification =>
          completeVerified(supervisor, querySupervisor, institutionId, period, seqNr, uri) {
            val verified = verification.verified
            val fSubmissionManager = (supervisor ? FindProcessingActor(SubmissionManager.name, SubmissionId(institutionId, period, seqNr))).mapTo[ActorRef]
            val subId = SubmissionId(institutionId, period, seqNr)
            val fValidator = fHmdaFileValidator(supervisor, subId)
            val editType: ValidationErrorType = if (verificationType == "quality") Quality else Macro

            val fVerification = for {
              replyTo <- fSubmissionManager
              va <- fValidator
              v <- (va ? VerifyEdits(editType, verified, replyTo)).mapTo[SubmissionStatus]
            } yield v

            onComplete(fVerification) {
              case Success(status) =>
                complete(ToResponseMarshallable(EditsVerifiedResponse(verified, status)))
              case Failure(error) => completeWithInternalError(uri, error)
            }
          }
        }
      }
    }

  /////// Helper Methods ///////
  private def fHmdaFileValidator(supervisor: ActorRef, submissionId: SubmissionId): Future[ActorRef] =
    (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]

  private def getStatusAndValidationState(supervisor: ActorRef, submissionId: SubmissionId)(implicit ec: ExecutionContext): Future[(HmdaFileValidationState, SubmissionStatus)] = {
    val fValidator = fHmdaFileValidator(supervisor, submissionId)
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]

    for {
      sa <- fSubmissionsActor
      s <- (sa ? GetSubmissionById(submissionId)).mapTo[Submission]
      va <- fValidator
      vs <- (va ? GetState).mapTo[HmdaFileValidationState]
    } yield (vs, s.status)

  }

  private def getValidationState(supervisor: ActorRef, institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[HmdaFileValidationState] = {
    val fValidator = fHmdaFileValidator(supervisor, SubmissionId(institutionId, period, seqNr))
    for {
      s <- fValidator
      xs <- (s ? GetState).mapTo[HmdaFileValidationState]
    } yield xs
  }

}
