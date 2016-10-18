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
import hmda.model.fi.SubmissionId
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.HmdaFileValidator
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.engine.{ Macro, Quality, Syntactical, Validity }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SubmissionEditPaths
    extends InstitutionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits
  def submissionEditsPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "edits") { (period, seqNr) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val fEditChecks = getValidationState(institutionId, period, seqNr)

          val fSummaryEdits = fEditChecks.map { editChecks =>
            val s = validationErrorsToEditResults(editChecks.larSyntactical ++ editChecks.tsSyntactical, Syntactical)
            val v = validationErrorsToEditResults(editChecks.larValidity ++ editChecks.tsValidity, Validity)
            val q = validationErrorsToEditResults(editChecks.larQuality ++ editChecks.tsQuality, Quality)
            val m = validationErrorsToEditResults(editChecks.larMacro ++ editChecks.tsMacro, Macro)
            SummaryEditResults(s, v, q, m)
          }

          onComplete(fSummaryEdits) {
            case Success(edits) =>
              complete(ToResponseMarshallable(edits))
            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }

    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/<editType>
  def submissionSingleEditPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / Segment) { (period, seqNr, editType) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val fValidationState = getValidationState(institutionId, period, seqNr)

          val fSingleEdits = fValidationState.map { editChecks =>
            editType match {
              case "syntactical" =>
                validationErrorsToEditResults(editChecks.larSyntactical ++ editChecks.tsSyntactical, Syntactical)
              case "validity" =>
                validationErrorsToEditResults(editChecks.larValidity ++ editChecks.tsValidity, Validity)
              case "quality" =>
                validationErrorsToEditResults(editChecks.larQuality ++ editChecks.tsQuality, Quality)
              case "macro" =>
                validationErrorsToEditResults(editChecks.larMacro ++ editChecks.tsMacro, Macro)
            }
          }

          onComplete(fSingleEdits) {
            case Success(edits) =>
              complete(ToResponseMarshallable(edits))
            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
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
