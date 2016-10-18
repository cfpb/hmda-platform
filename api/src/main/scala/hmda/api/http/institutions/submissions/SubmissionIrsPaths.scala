package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Submission }
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.GetFilingByPeriod
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetLatestSubmission }
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.parsing.json.JSON
import scala.util.{ Failure, Success }

trait SubmissionIrsPaths
    extends InstitutionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  case class Receipt(timestamp: Long, receipt: String)
  implicit val receiptProtocol = jsonFormat2(Receipt.apply)

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs
  def irsPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "irs") { (period, submissionId) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")

          val irsJson = Source.fromFile("src/main/scala/hmda/api/http/institutions/submissions/tempJson/irs.json").getLines.mkString
          val response = HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, irsJson))

          complete(response)
        } ~
          timedPost { uri =>
            entity(as[JsObject]) { json =>
              val verified = json.fields("verified").asInstanceOf[JsBoolean]
              verified match {
                case JsTrue => complete(Receipt(System.currentTimeMillis(), "recieptHash"))
                case JsFalse => complete(Receipt(0L, ""))
              }
            }
          }
      }
    }
}
