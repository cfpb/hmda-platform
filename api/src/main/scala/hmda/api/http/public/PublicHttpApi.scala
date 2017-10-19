package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import akka.http.scaladsl.server.Directives._
import hmda.api.model.public.ULIModel.{ Loan, ULI, ULICheck, ULIValidated }
import hmda.api.protocol.public.ULIProtocol._
import hmda.validation.engine.lar.ULI._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable

import scala.concurrent.ExecutionContext

trait PublicHttpApi extends PublicLarHttpApi with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  val publicHttpRoutes =
    extractExecutionContext { executor =>
      implicit val ec: ExecutionContext = executor
      encodeResponse {
        pathPrefix("institutions" / Segment) { instId =>
          modifiedLar(instId)
        } ~
          pathPrefix("uli") {
            path("check-digit") {
              timedPost { _ =>
                entity(as[Loan]) { loan =>
                  val loanId = loan.loanId
                  val check = checkDigit(loanId)
                  val uli = ULI(loanId, check.toInt, loanId + check)
                  complete(ToResponseMarshallable(uli))
                }
              }
            } ~
              path("validate") {
                timedPost { _ =>
                  entity(as[ULICheck]) { uc =>
                    val uli = uc.uli
                    val isValid = validateULI(uli)
                    val validated = ULIValidated(isValid)
                    complete(ToResponseMarshallable(validated))
                  }
                }
              }
          }
      }
    }
}
