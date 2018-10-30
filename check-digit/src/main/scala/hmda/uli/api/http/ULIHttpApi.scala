package hmda.uli.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.stream.scaladsl.{Sink, Source}
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.ErrorResponse
import hmda.uli.api.model.ULIModel._
import hmda.uli.validation.ULI._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import hmda.util.streams.FlowUtils._

import scala.util.{Failure, Success, Try}

trait ULIHttpApi extends HmdaTimeDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  val uliHttpRoutes =
    encodeResponse {
      pathPrefix("udi") {
        path("checkDigit") {
          timedPost { uri =>
            entity(as[Loan]) { loan =>
              val loanId = loan.loanId
              val maybeDigit = Try(checkDigit(loanId))
              maybeDigit match {
                case Success(digit) =>
                  val uli = ULI(loan.loanId, digit, loan.loanId + digit)
                  complete(ToResponseMarshallable(uli))
                case Failure(error) =>
                  val errorResponse =
                    ErrorResponse(400, error.getLocalizedMessage, uri.path)
                  complete(ToResponseMarshallable(
                    StatusCodes.BadRequest -> errorResponse))
              }
            } ~
              fileUpload("file") {
                case (_, byteSource) =>
                  val checkDigitF =
                    processLoanIdFile(byteSource).runWith(Sink.seq)
                  onComplete(checkDigitF) {
                    case Success(checkDigits) => {
                      complete(ToResponseMarshallable(
                        LoanCheckDigitResponse(checkDigits)))
                    }
                    case Failure(error) =>
                      log.error(error.getLocalizedMessage)
                      val errorResponse =
                        ErrorResponse(400, error.getLocalizedMessage, uri.path)
                      complete(ToResponseMarshallable(
                        StatusCodes.BadRequest -> errorResponse))
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
                  val headerSource = Source.fromIterator(() =>
                    List("loanId,checkDigit,uli\n").toIterator)
                  val checkDigit = processLoanIdFile(byteSource)
                    .map(l => l.toCSV)
                    .map(l => l + "\n")
                    .map(s => ByteString(s))

                  val csv =
                    headerSource.map(s => ByteString(s)).concat(checkDigit)
                  complete(
                    HttpEntity.Chunked.fromData(
                      `text/csv`.toContentType(HttpCharsets.`UTF-8`),
                      csv))

                case _ =>
                  complete(ToResponseMarshallable(StatusCodes.BadRequest))
              }
            }
          } ~
          path("validate") {
            timedPost { uri =>
              entity(as[ULICheck]) { uc =>
                val uli = uc.uli
                val isValid = Try(validateULI(uli))
                isValid match {
                  case Success(value) =>
                    val validated = ULIValidated(value)
                    complete(ToResponseMarshallable(validated))
                  case Failure(error) =>
                    val errorResponse =
                      ErrorResponse(400, error.getLocalizedMessage, uri.path)
                    complete(ToResponseMarshallable(
                      StatusCodes.BadRequest -> errorResponse))
                }
              } ~
                fileUpload("file") {
                  case (_, byteSource) =>
                    val validatedF =
                      processUliFile(byteSource).runWith(Sink.seq)
                    onComplete(validatedF) {
                      case Success(validated) =>
                        complete(ToResponseMarshallable(
                          ULIBatchValidatedResponse(validated)))
                      case Failure(error) =>
                        log.error(error.getLocalizedMessage)
                        val errorResponse =
                          ErrorResponse(400,
                                        error.getLocalizedMessage,
                                        uri.path)
                        complete(ToResponseMarshallable(
                          StatusCodes.BadRequest -> errorResponse))
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
                  val headerSource =
                    Source.fromIterator(() => List("uli,isValid\n").toIterator)
                  val validated = processUliFile(byteSource)
                    .map(u => u.toCSV)
                    .map(l => l + "\n")
                    .map(s => ByteString(s))

                  val csv =
                    headerSource.map(s => ByteString(s)).concat(validated)
                  complete(
                    HttpEntity.Chunked.fromData(
                      `text/csv`.toContentType(HttpCharsets.`UTF-8`),
                      csv))

                case _ =>
                  complete(ToResponseMarshallable(StatusCodes.BadRequest))
              }
            }
          }
      }
    }

  private def processLoanIdFile(byteSource: Source[ByteString, Any]) = {
    byteSource
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .filter(loanId => loanIdIsValidLength(loanId))
      .filter(loanId => isAlphanumeric(loanId))
      .map { loanId =>
        val digit = checkDigit(loanId)
        ULI(loanId, digit, loanId + digit)
      }
  }

  private def processUliFile(byteSource: Source[ByteString, Any]) = {
    byteSource
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .filter(uli => uliIsValidLength(uli))
      .filter(uli => isAlphanumeric(uli))
      .map(uli => (uli, validateULI(uli)))
      .map(validated => ULIBatchValidated(validated._1, validated._2))
  }

}
