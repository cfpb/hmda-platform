package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import hmda.model.fi.{ Signed, SubmissionId, Validated }
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.SubmissionManager
import hmda.query.HmdaQuerySupervisor.FindHmdaFilingView
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }

import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext

trait SubmissionSignPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/sign
  // NOTE:  This is currently a mocked, static endpoint
  def submissionSignPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "sign") { (period, id) =>
      timedGet { uri =>
        complete(ToResponseMarshallable(Receipt(0L, "", Validated)))
      } ~
        timedPost { uri =>
          entity(as[JsObject]) { json =>
            val verified = json.fields("signed").asInstanceOf[JsBoolean]
            val supervisor = system.actorSelection("/user/supervisor")
            val submissionId = SubmissionId(institutionId, period, id)
            val fProcessingActor = (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]

            verified match {
              case JsTrue =>
                val managerF = for {
                  manager <- fProcessingActor
                } yield manager

                onComplete(managerF) {
                  case Success(manager) =>
                    manager ! hmda.persistence.processing.ProcessingMessages.Signed(submissionId)
                    complete(ToResponseMarshallable(Receipt(System.currentTimeMillis(), "receiptHash", Signed)))
                  case Failure(error) =>
                    completeWithInternalError(uri, error)
                }

              case JsFalse =>
                complete(ToResponseMarshallable(Receipt(0l, "", Validated)))
            }

          }
        }
    }
}
