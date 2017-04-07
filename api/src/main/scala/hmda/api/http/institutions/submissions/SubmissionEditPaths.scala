package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSelection, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol }
import hmda.model.fi.{ Submission, SubmissionId, SubmissionStatus }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.HmdaFileValidator
import hmda.persistence.processing.HmdaFileValidator._
import hmda.validation.engine._

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
  def submissionEditsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits") { (period, seqNr) =>
      timedGet { uri =>
        completeVerified(institutionId, period, seqNr, uri) {
          val fState = getStatusAndValidationState(SubmissionId(institutionId, period, seqNr))
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
  def submissionEditCsvPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / "csv") { (period, seqNr) =>
      timedGet { uri =>
        completeVerified(institutionId, period, seqNr, uri) {
          val fValidationState = getValidationState(institutionId, period, seqNr)
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
  def submissionSingleEditPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / svqmRegex) { (period, seqNr, editType) =>
      timedGet { uri =>
        completeVerified(institutionId, period, seqNr, uri) {
          val fState = getStatusAndValidationState(SubmissionId(institutionId, period, seqNr))
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
  def editFailureDetailsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / editNameRegex) { (period, seqNr, editName) =>
      timedGet { uri =>
        completeVerified(institutionId, period, seqNr, uri) {
          parameters('page.as[Int] ? 1) { (page: Int) =>
            val fValidator: Future[ActorRef] = fHmdaFileValidator(SubmissionId(institutionId, period, seqNr))
            val fPaginatedErrors: Future[(PaginatedErrors, HmdaFileValidationState)] = for {
              va <- fValidator
              vs <- (va ? GetState).mapTo[HmdaFileValidationState]
              p <- (va ? GetNamedErrorResultsPaginated(editName, page)).mapTo[PaginatedErrors]
            } yield (p, vs)

            onComplete(fPaginatedErrors) {
              case Success((errorCollection, vs)) =>
                val rows: Seq[EditResultRow] = errorCollection.errors.map(validationErrorToResultRow(_, vs))
                val result = EditResult(editName, rows, uri.path.toString, page, errorCollection.totalErrors)
                complete(ToResponseMarshallable(result))
              case Failure(error) => completeWithInternalError(uri, error)
            }
          }
        }
      }
    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/quality|macro
  private val editTypeRegex = new Regex("quality|macro")
  def verifyEditsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / editTypeRegex) { (period, seqNr, verificationType) =>
      timedPost { uri =>
        entity(as[EditsVerification]) { verification =>
          completeVerified(institutionId, period, seqNr, uri) {
            val verified = verification.verified
            val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]
            val subId = SubmissionId(institutionId, period, seqNr)
            val fValidator = fHmdaFileValidator(subId)
            val editType: ValidationErrorType = if (verificationType == "quality") Quality else Macro

            val fSubmissions = for {
              va <- fValidator
              v <- (va ? VerifyEdits(editType, verified)).mapTo[EditsVerified]
              sa <- fSubmissionsActor
              s <- (sa ? GetSubmissionById(subId)).mapTo[Submission]
            } yield s

            onComplete(fSubmissions) {
              case Success(submission) =>
                complete(ToResponseMarshallable(EditsVerifiedResponse(verified, submission.status)))
              case Failure(error) => completeWithInternalError(uri, error)
            }
          }
        }
      }
    }

  /////// Helper Methods ///////
  private def supervisor: ActorSelection = system.actorSelection("/user/supervisor")
  private def fHmdaFileValidator(submissionId: SubmissionId): Future[ActorRef] =
    (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]

  private def getStatusAndValidationState(submissionId: SubmissionId)(implicit ec: ExecutionContext): Future[(HmdaFileValidationState, SubmissionStatus)] = {
    val fValidator = fHmdaFileValidator(submissionId)
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]

    for {
      sa <- fSubmissionsActor
      s <- (sa ? GetSubmissionById(submissionId)).mapTo[Submission]
      va <- fValidator
      vs <- (va ? GetState).mapTo[HmdaFileValidationState]
    } yield (vs, s.status)

  }

  private def getValidationState(institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[HmdaFileValidationState] = {
    val fValidator = fHmdaFileValidator(SubmissionId(institutionId, period, seqNr))
    for {
      s <- fValidator
      xs <- (s ? GetState).mapTo[HmdaFileValidationState]
    } yield xs
  }

}
