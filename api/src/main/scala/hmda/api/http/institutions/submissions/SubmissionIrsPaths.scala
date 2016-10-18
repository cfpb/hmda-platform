package hmda.api.http.institutions.submissions

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }
import java.io.File

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.Try

trait SubmissionIrsPaths
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

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs
  def submissionIrsPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "irs") { (period, submissionId) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")

          val partialPath = "src/main/scala/hmda/api/http/institutions/submissions/tempJson/irs.json"
          val source = Try(Source.fromFile(new File(partialPath))).getOrElse(Source.fromFile(new File("api/" + partialPath)))

          val irsJson = source.getLines.mkString
          val response = HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, irsJson))

          complete(response)
        } ~
          timedPost { uri =>
            entity(as[JsObject]) { json =>
              val verified = json.fields("verified").asInstanceOf[JsBoolean]
              verified match {
                case JsTrue => complete(ToResponseMarshallable(Receipt(System.currentTimeMillis(), "receiptHash")))
                case JsFalse => complete(ToResponseMarshallable(Receipt.empty))
              }
            }
          }
      }
    }
}
