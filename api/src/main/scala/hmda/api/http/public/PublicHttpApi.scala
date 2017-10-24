package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import akka.http.scaladsl.server.Directives._
import hmda.api.model.public.ULIModel._
import hmda.validation.engine.lar.ULI._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.scaladsl.Sink
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.api.protocol.public.ULIProtocol
import hmda.api.util.FlowUtils

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait PublicHttpApi extends PublicLarHttpApi with HmdaCustomDirectives with ApiErrorProtocol with ULIProtocol with FlowUtils {
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
                  } ~
                    fileUpload("file") {
                      case (_, byteSource) =>
                        val validatedF = byteSource
                          .via(framing)
                          .map(_.utf8String)
                          .map(uli => (uli, validateULI(uli)))
                          .map(validated => ULIBatchValidated(validated._1, validated._2))
                          .runWith(Sink.seq)

                        onComplete(validatedF) {
                          case Success(validated) =>
                            complete(ToResponseMarshallable(ULIBatchValidatedResponse(validated)))
                          case Failure(error) =>
                            log.error(error.getLocalizedMessage)
                            complete(ToResponseMarshallable(StatusCodes.InternalServerError))
                        }

                      case _ =>
                        complete(ToResponseMarshallable(StatusCodes.BadRequest))
                    }
                }
              }
          }
      }
    }
}
