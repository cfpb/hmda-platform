package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.SubmissionManager
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }

import scala.util.{ Failure, Success }
import scala.concurrent.{ ExecutionContext, Future }

trait SubmissionSignPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with RequestVerificationUtils
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/sign
  def submissionSignPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "sign") { (period, id) =>
      val supervisor = system.actorSelection("/user/supervisor")
      val submissionId = SubmissionId(institutionId, period, id)
      timedGet { uri =>
        completeVerified(institutionId, period, id, uri) {
          completeWithSubmissionReceipt(submissionId, uri)
        }
      } ~
        timedPost { uri =>
          completeVerified(institutionId, period, id, uri) {
            entity(as[JsObject]) { json =>
              val verified = json.fields("signed").asInstanceOf[JsBoolean]
              verified match {
                case JsTrue =>
                  val fProcessingActor: Future[ActorRef] = (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
                  val fSign = for {
                    actor <- fProcessingActor
                    s <- actor ? hmda.persistence.processing.ProcessingMessages.Signed
                  } yield s
                  onComplete(fSign) {
                    case Success(Some(_)) => completeWithSubmissionReceipt(submissionId, uri)
                    case Success(_) =>
                      val errorResponse = ErrorResponse(400, "Illegal State: Submission must be Validated or ValidatedWithErrors to sign", uri.path)
                      complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
                    case Failure(error) => completeWithInternalError(uri, error)
                  }

                case JsFalse =>
                  val errorResponse = ErrorResponse(400, "Illegal Argument: signed = false", uri.path)
                  complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
              }

            }
          }
        }
    }

  private def completeWithSubmissionReceipt(subId: SubmissionId, uri: Uri)(implicit ec: ExecutionContext) = {
    val supervisor = system.actorSelection("/user/supervisor")
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, subId.institutionId, subId.period)).mapTo[ActorRef]
    val fSubmission = for {
      a <- fSubmissionsActor
      s <- (a ? GetSubmissionById(subId)).mapTo[Submission]
    } yield s

    onComplete(fSubmission) {
      case Success(sub) =>
        complete(ToResponseMarshallable(Receipt(sub.end, sub.receipt, sub.status)))
      case Failure(error) => completeWithInternalError(uri, error)
    }
  }
}
