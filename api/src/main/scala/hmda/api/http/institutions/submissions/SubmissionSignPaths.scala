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
import hmda.model.fi.{ IRSVerified, Signed, SubmissionId }
import hmda.persistence.HmdaSupervisor.FindHmdaFiling
import hmda.persistence.processing.HmdaFiling.SaveLars
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
  def submissionSignPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "sign") { (period, id) =>
      extractExecutionContext { executor =>
        implicit val ec: ExecutionContext = executor
        timedGet { uri =>
          complete(ToResponseMarshallable(Receipt(0L, "", IRSVerified)))
        } ~
          timedPost { uri =>
            entity(as[JsObject]) { json =>
              val verified = json.fields("signed").asInstanceOf[JsBoolean]

              val supervisor = system.actorSelection("/user/supervisor")
              val querySupervisor = system.actorSelection("/user/query-supervisor")

              val hmdaFilingF = (supervisor ? FindHmdaFiling(period)).mapTo[ActorRef]
              val hmdaFilingViewF = (querySupervisor ? FindHmdaFilingView(period)).mapTo[ActorRef]

              verified match {
                case JsTrue =>
                  val filingF = for {
                    filing <- hmdaFilingF
                    filingView <- hmdaFilingViewF
                  } yield filing

                  onComplete(filingF) {
                    case Success(filing) =>
                      val submissionId = SubmissionId(institutionId, period, id)
                      filing ! SaveLars(submissionId)
                      complete(ToResponseMarshallable(Receipt(System.currentTimeMillis(), "receiptHash", Signed)))
                    case Failure(error) =>
                      completeWithInternalError(uri, error)
                  }

                case JsFalse =>
                  complete(ToResponseMarshallable(Receipt(0l, "", IRSVerified)))
              }

            }
          }
      }
    }
}
