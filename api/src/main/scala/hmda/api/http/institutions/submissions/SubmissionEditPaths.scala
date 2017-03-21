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
import hmda.model.fi.{ Submission, SubmissionId }
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
          val fEditChecks = getValidationState(institutionId, period, seqNr)
          parameters("format".?) { format: Option[String] =>
            val fEditSummary: Future[SummaryEditResults] = fEditChecks.map { e =>
              val s = validationErrorsToEditResults(e, e.tsSyntactical, e.larSyntactical, Syntactical)
              val v = validationErrorsToEditResults(e, e.tsValidity, e.larValidity, Validity)
              val q = validationErrorsToQualityEditResults(e, e.tsQuality, e.larQuality)
              val m = validationErrorsToMacroResults(e, e.larMacro)
              SummaryEditResults(s, v, q, m)
            }

            onComplete(fEditSummary) {
              case Success(edits) =>
                if (format.getOrElse("") == "csv") complete(edits.toCsv)
                else complete(ToResponseMarshallable(edits))
              case Failure(error) => completeWithInternalError(uri, error)
            }
          }
        }
      }
    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/<editType>
  def submissionSingleEditPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / Segment) { (period, seqNr, editType) =>
      timedGet { uri =>
        parameters("format".?) { format: Option[String] =>
          completeVerified(institutionId, period, seqNr, uri) {
            val fValidationState = getValidationState(institutionId, period, seqNr)
            completeValidationState(editType, fValidationState, uri, format.getOrElse(""))
          }
        }
      }
    }

  private val editTypeRegex = new Regex("quality|macro")
  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/quality|macro
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

  private def completeValidationState(editType: String, fValidationState: Future[HmdaFileValidationState], uri: Uri, format: String)(implicit ec: ExecutionContext) = {
    val fSingleEdits = fValidationState.map { e =>
      editType match {
        case "syntactical" => validationErrorsToEditResults(e, e.tsSyntactical, e.larSyntactical, Syntactical)
        case "validity" => validationErrorsToEditResults(e, e.tsValidity, e.larValidity, Validity)
        case "quality" => validationErrorsToEditResults(e, e.tsQuality, e.larQuality, Quality)
        case "macro" => validationErrorsToMacroResults(e, e.larMacro)
      }
    }

    onComplete(fSingleEdits) {
      case Success(edits: MacroResults) =>
        if (format == "csv") complete("editType, editId\n" + edits.toCsv)
        else complete(ToResponseMarshallable(edits))
      case Success(edits: EditResults) =>
        if (format == "csv") complete("editType, editId, loanId\n" + edits.toCsv(editType))
        else complete(ToResponseMarshallable(edits))
      case Success(_) => completeWithInternalError(uri, new IllegalStateException)
      case Failure(error) => completeWithInternalError(uri, error)
    }
  }

  private def getValidationState(institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[HmdaFileValidationState] = {
    val fValidator = fHmdaFileValidator(SubmissionId(institutionId, period, seqNr))
    for {
      s <- fValidator
      xs <- (s ? GetState).mapTo[HmdaFileValidationState]
    } yield xs
  }

}
