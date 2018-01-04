package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.{ ByteString, Timeout }
import hmda.api.http.HmdaCustomDirectives
import akka.http.scaladsl.server.Directives._
import hmda.api.model.public.ULIModel._
import hmda.validation.engine.lar.ULI._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpCharsets, HttpEntity, StatusCodes }
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.stream.scaladsl.{ Sink, Source }
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.api.protocol.public.ULIProtocol
import hmda.api.util.FlowUtils

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait ULIHttpApi extends HmdaCustomDirectives with ApiErrorProtocol with ULIProtocol with FlowUtils {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  val uliHttpRoutes =
    encodeResponse {
      pathPrefix("uli") {
        path("checkDigit") {
          timedPost { _ =>
            entity(as[Loan]) { loan =>
              val digit = checkDigit(loan.loanId)
              val uli = ULI(loan.loanId, digit, loan.loanId + digit)
              complete(ToResponseMarshallable(uli))
            } ~
              fileUpload("file") {
                case (_, byteSource) =>
                  val checkDigitF = processLoanIdFile(byteSource).runWith(Sink.seq)
                  onComplete(checkDigitF) {
                    case Success(checkDigits) => {
                      complete(ToResponseMarshallable(LoanCheckDigitResponse(checkDigits)))
                    }
                    case Failure(error) =>
                      log.error(error.getLocalizedMessage)
                      complete(ToResponseMarshallable(StatusCodes.InternalServerError))
                  }
                case _ =>
                  complete(ToResponseMarshallable(StatusCodes.BadRequest))
              }
          }
        } ~
          path("checkDigit" / "csv") {
            timedPost { _ =>
              fileUpload("file") {
                case (_, byteSource) =>
                  val headerSource = Source.fromIterator(() => List("loanId,checkDigit,uli\n").toIterator)
                  val checkDigit = processLoanIdFile(byteSource)
                    .map(l => l.toCSV)
                    .map(l => l + "\n")
                    .map(s => ByteString(s))

                  val csv = headerSource.map(s => ByteString(s)).concat(checkDigit)
                  complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))

                case _ =>
                  complete(ToResponseMarshallable(StatusCodes.BadRequest))
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
                    val validatedF = processUliFile(byteSource).runWith(Sink.seq)
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
          } ~
          path("validate" / "csv") {
            timedPost { _ =>
              fileUpload("file") {
                case (_, byteSource) =>
                  val headerSource = Source.fromIterator(() => List("uli,isValid\n").toIterator)
                  val validated = processUliFile(byteSource)
                    .map(u => u.toCSV)
                    .map(l => l + "\n")
                    .map(s => ByteString(s))

                  val csv = headerSource.map(s => ByteString(s)).concat(validated)
                  complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))

                case _ =>
                  complete(ToResponseMarshallable(StatusCodes.BadRequest))
              }
            }
          }
      }
    }

  private def processLoanIdFile(byteSource: Source[ByteString, Any]) = {
    byteSource
      .via(framing)
      .map(_.utf8String)
      .map { loanId =>
        val digit = checkDigit(loanId)
        ULI(loanId, digit, loanId + digit)
      }
  }

  private def processUliFile(byteSource: Source[ByteString, Any]) = {
    byteSource
      .via(framing)
      .map(_.utf8String)
      .map(uli => (uli, validateULI(uli)))
      .map(validated => ULIBatchValidated(validated._1, validated._2))
  }
}
