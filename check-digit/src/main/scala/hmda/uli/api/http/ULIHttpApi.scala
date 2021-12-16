package hmda.uli.api.http

import akka.NotUsed
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.uli.api.model.ULIModel._
import hmda.uli.api.model.ULIValidationErrorMessages.{invalidLoanIdLengthMessage, nonAlpanumericLoanIdMessage}
import hmda.uli.validation.ULI._
import hmda.util.http.FilingResponseUtils.failedResponse
import hmda.util.streams.FlowUtils._
import io.circe.generic.auto._
import org.slf4j.Logger

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object ULIHttpApi {
  def create(log: Logger): Route = new ULIHttpApi(log).uliHttpRoutes
}
private class ULIHttpApi(log: Logger) {
  val uliHttpRoutes: Route =
    encodeResponse {
      pathPrefix("uli") {
        path("checkDigit") {
          extractUri { uri =>
            entity(as[Loan]) { loan =>
              val loanId     = loan.loanId
              val maybeDigit = Try(checkDigit(loanId))
              maybeDigit match {
                case Success(digit) =>
                  val uli = ULI(loan.loanId, digit, loan.loanId + digit)
                  log.info(
                    "API Check Digit Request: " + maybeDigit.toString + "\n" +
                      " Check Digit Result: " + digit + "\n" +
                      " ULI: " + uli.toString
                  )
                  complete(ToResponseMarshallable(uli))
                case Failure(error) =>
                  log.info(
                    "API Check Digit Request Failed: " + maybeDigit + "\n" +
                      " Error: " + error.toString
                  )
                  failedResponse(BadRequest, uri, error)
              }
            } ~
              fileUpload("file") {
                case (_, byteSource) =>
                  val lStart  = ByteString.fromString("{\"loanIds\":[")
                  val lMiddle = ByteString.fromString(",")
                  val lEnd    = ByteString.fromString("]}")
                  val withLoanWrapper: Flow[ByteString, ByteString, NotUsed] =
                    Flow[ByteString]
                      .intersperse(lStart, lMiddle, lEnd)
                  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
                    EntityStreamingSupport
                      .json()
                      .withFramingRenderer(withLoanWrapper)

                  val checkDigitSource =
                    processLoanIdFile(byteSource)

                  log.info("Check Digit File Request Received.")

                  complete(checkDigitSource)
                case _ =>
                  complete(BadRequest)
              }
          }
        } ~
          path("checkDigit" / "csv") {
            toStrictEntity(35.seconds) {
              post {
                respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
                  fileUpload("file") {
                    case (_, byteSource) =>
                      val headerSource = Source.fromIterator(() => List("loanId,checkDigit,uli\n").toIterator)
                      val checkDigit = processLoanIdFile(byteSource)
                        .map(l => l.toCSV)
                        .map(l => l + "\n")
                        .map(s => ByteString(s))

                      val csv =
                        headerSource.map(s => ByteString(s)).concat(checkDigit)

                      log.info("CSV Check Digit File Request Received.")

                      complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))

                    case _ =>
                      complete(BadRequest)
                  }
                }
              }
            }
          } ~
          path("validate") {
            extractUri { uri =>
              entity(as[ULICheck]) { uc =>
                val uli     = uc.uli
                val isValid = Try(validateULI(uli))
                isValid match {
                  case Success(value) =>
                    val validated = ULIValidated(value)
                    log.info(
                      "API CheckDigit > Validate " + isValid.toString + "\n" +
                        "Validate Response: " + validated.toString
                    )
                    complete(ToResponseMarshallable(validated))
                  case Failure(error) =>
                    log.info(
                      "API CheckDigit (Validate) Failed: " + isValid.toString + "\n" +
                        "Error: " + error.toString
                    )
                    failedResponse(BadRequest, uri, error)
                }
              } ~
                fileUpload("file") {
                  case (_, byteSource) =>
                    val uStart  = ByteString.fromString("{\"ulis\":[")
                    val uMiddle = ByteString.fromString(",")
                    val uEnd    = ByteString.fromString("]}")
                    val withUliWrapper: Flow[ByteString, ByteString, NotUsed] =
                      Flow[ByteString]
                        .intersperse(uStart, uMiddle, uEnd)
                    implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
                      EntityStreamingSupport
                        .json()
                        .withFramingRenderer(withUliWrapper)

                    val validatedStream =
                      processUliFile(byteSource)

                    log.info("Check Digit > Validate File Request Received.")

                    complete(validatedStream)

                  case _ =>
                    complete(BadRequest)
                }
            }
          } ~
          path("validate" / "csv") {
            toStrictEntity(35.seconds) {
              post {
                respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
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

                      log.info("CSV Check Digit > Validate File Request Received.")

                      complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))

                    case _ =>
                      complete(BadRequest)
                  }
                }
              }
            }
          }
      }
    }

  private def processLoanIdFile(byteSource: Source[ByteString, Any]) =
    byteSource
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map { loanId =>
        if(!loanIdIsValidLength(loanId)) {
          ULI(loanId, "Error", invalidLoanIdLengthMessage)
        }
        else if(!isAlphanumeric(loanId)) {
          ULI(loanId, "Error", nonAlpanumericLoanIdMessage)
        }
        else  {
          val digit = checkDigit(loanId)
          ULI(loanId, digit, loanId + digit)
        }
      }

  private def processUliFile(byteSource: Source[ByteString, Any]) =
    byteSource
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(uli => (uli, validateCheckDigitULI(uli)))
      .map(validated => ULIBatchValidated(validated._1, validated._2))

}