package hmda.api.http.institutions.submissions

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }

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
  def submissionSignPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "sign") { (period, submissionId) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")

          complete(ToResponseMarshallable(Receipt.empty))
        } ~
          timedPost { uri =>
            entity(as[JsObject]) { json =>
              val verified = json.fields("signed").asInstanceOf[JsBoolean]
              verified match {
                case JsTrue => complete(ToResponseMarshallable(Receipt(System.currentTimeMillis(), "receiptHash")))
                case JsFalse => complete(ToResponseMarshallable(Receipt.empty))
              }
            }
          }
      }
    }
}
