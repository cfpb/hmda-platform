package hmda.api.http.institutions.submissions

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpCharsets, HttpEntity }
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol }
import hmda.model.fi.{ SubmissionId, SubmissionStatus }
import hmda.model.validation.{ Macro, Quality, ValidationErrorType }
import hmda.persistence.messages.CommonMessages.{ Event, GetState }
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionStatus
import hmda.persistence.messages.commands.processing.HmdaFileValidatorState.{ HmdaVerificationState, QMState, SVState }
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
        completeVerified(supervisor, institutionId, period, seqNr, uri) {
          val submissionId = SubmissionId(institutionId, period, seqNr)
          val fValidator = fHmdaFileValidator(supervisor, submissionId)
          val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]

          val fState = for {
            sa <- fSubmissionsActor
            status <- (sa ? GetSubmissionStatus(submissionId)).mapTo[SubmissionStatus]
            va <- fValidator
            vs <- (va ? GetState).mapTo[HmdaVerificationState]
            svState <- (va ? GetSVState).mapTo[SVState]
            qmState <- (va ? GetQMState).mapTo[QMState]
          } yield (vs.qualityVerified, vs.macroVerified, status, svState, qmState)

          onComplete(fState) {
            case Success((qv, mv, status, svState, qmState)) =>
              val s = EditCollection(editInfos(svState.syntacticalEdits))
              val v = EditCollection(editInfos(svState.validityEdits))
              val q = VerifiableEditCollection(qv, editInfos(qmState.qualityEdits))
              val m = VerifiableEditCollection(mv, editInfos(qmState.macroEdits))
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
        completeVerified(supervisor, institutionId, period, seqNr, uri) {
          val csv = csvResultStream(validationEventStream(SubmissionId(institutionId, period, seqNr))).map(ByteString(_))
          complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))
        }
      }
    }

  // /institutions/<institution>/filings/<period>/submissions/<submissionId>/edits/<edit>
  private val editNameRegex: Regex = new Regex("""[SVQ]\d\d\d""")

  def editFailureDetailsPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / editNameRegex) { (period, seqNr, editName) =>
      timedGet { uri =>
        completeVerified(supervisor, institutionId, period, seqNr, uri) {
          parameters('page.as[Int] ? 1) { (page: Int) =>
            val submissionId = SubmissionId(institutionId, period, seqNr)
            val eventStream = validationEventStream(submissionId)
            val fValidator: Future[ActorRef] = fHmdaFileValidator(supervisor, submissionId)

            val fPaginatedErrors = for {
              va <- fValidator
              vs <- (va ? GetState).mapTo[HmdaVerificationState]
              errorCollection <- (va ? GetNamedErrorResultsPaginated(editName, page)).mapTo[PaginatedErrors]
              rows <- resultRowsFromCollection(errorCollection.errors, vs.ts, eventStream)
            } yield (rows, errorCollection.totalErrors)

            onComplete(fPaginatedErrors) {
              case Success((jsonRows, total)) =>
                val result = EditResult(editName, jsonRows, uri.path.toString, page, total)
                complete(ToResponseMarshallable(result))
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
          completeVerified(supervisor, institutionId, period, seqNr, uri) {
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

  private def validationEventStream(submissionId: SubmissionId): Source[Event, NotUsed] = {
    val persistenceId = s"${HmdaFileValidator.name}-$submissionId"
    events(persistenceId)
  }

}
