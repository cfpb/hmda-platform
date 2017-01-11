package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
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
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.HmdaFileValidator
import hmda.persistence.processing.HmdaFileValidator.{ HmdaFileValidationState, JustifyMacroEdit, MacroEditJustified }
import hmda.validation.engine.{ Macro, Quality, Syntactical, Validity }

import scala.concurrent.{ ExecutionContext, Future }
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

          parameters("format".?, "sortBy".?) { (format: Option[String], sortBy: Option[String]) =>
            sortBy match {
              case Some("row") =>
                val fRowSummary: Future[RowResults] = fEditChecks.map { e =>
                  val tsErrors = e.tsSyntactical ++ e.tsValidity ++ e.tsQuality
                  val larErrors = e.larSyntactical ++ e.larValidity ++ e.larQuality
                  val macroErrors = e.larMacro
                  validationErrorsToRowResults(tsErrors, larErrors, macroErrors)
                }
                onComplete(fRowSummary) {
                  case Success(rows) => complete(ToResponseMarshallable(rows))
                  case Failure(error) => completeWithInternalError(uri, error)
                }

              case _ =>
                val fEditSummary: Future[SummaryEditResults] = fEditChecks.map { e =>
                  val s = validationErrorsToEditResults(e.tsSyntactical, e.larSyntactical, Syntactical)
                  val v = validationErrorsToEditResults(e.tsValidity, e.larValidity, Validity)
                  val q = validationErrorsToEditResults(e.tsQuality, e.larQuality, Quality)
                  val m = validationErrorsToMacroResults(e.larMacro)
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
    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/<editType>
  def submissionSingleEditPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / Segment) { (period, seqNr, editType) =>
      timedGet { uri =>
        parameters("format".?) { format =>

          completeVerified(institutionId, period, seqNr, uri) {
            val fValidationState = getValidationState(institutionId, period, seqNr)
            completeValidationState(editType, fValidationState, uri, format.getOrElse(""))
          }
        }
      } ~ timedPost { uri =>
        if (editType == "macro") {
          entity(as[MacroEditJustificationWithName]) { justifyEdit =>
            completeVerified(institutionId, period, seqNr, uri) {
              val supervisor = system.actorSelection("/user/supervisor")
              val submissionId = SubmissionId(institutionId, period, seqNr)
              val fHmdaFileValidator = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
              val fValidationState = for {
                a <- fHmdaFileValidator
                j <- (a ? JustifyMacroEdit(justifyEdit.edit, justifyEdit.justification)).mapTo[MacroEditJustified]
                state <- getValidationState(institutionId, period, seqNr)
              } yield state
              completeValidationState(editType, fValidationState, uri, "")
            }
          }
        } else {
          completeWithMethodNotAllowed(uri)
        }
      }
    }

  private def completeValidationState(editType: String, fValidationState: Future[HmdaFileValidationState], uri: Uri, format: String)(implicit ec: ExecutionContext) = {
    val fSingleEdits = fValidationState.map { editChecks =>
      editType match {
        case "syntactical" =>
          validationErrorsToEditResults(editChecks.tsSyntactical, editChecks.larSyntactical, Syntactical)
        case "validity" =>
          validationErrorsToEditResults(editChecks.tsValidity, editChecks.larValidity, Validity)
        case "quality" =>
          validationErrorsToEditResults(editChecks.tsQuality, editChecks.larQuality, Quality)
        case "macro" =>
          validationErrorsToMacroResults(editChecks.larMacro)
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
    val supervisor = system.actorSelection("/user/supervisor")
    val submissionID = SubmissionId(institutionId, period, seqNr)
    val fHmdaFileValidator = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionID)).mapTo[ActorRef]

    for {
      s <- fHmdaFileValidator
      xs <- (s ? GetState).mapTo[HmdaFileValidationState]
    } yield xs
  }

}
