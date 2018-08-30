package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.api.http.model.public.{
  LarValidateRequest,
  LarValidateResponse,
  SingleValidationErrorResult,
  ValidationErrorSummary
}
import hmda.parser.filing.lar.LarCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.codec.filing.LarCodec._
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.validation._
import hmda.parser.ParserErrorModel
import hmda.validation.engine.LarEngine._

import scala.concurrent.ExecutionContext

trait LarValidationHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  //lar/parse
  val parseLarRoute =
    path("parse") {
      timedPost { _ =>
        entity(as[LarValidateRequest]) { req =>
          LarCsvParser(req.lar) match {
            case Right(lar) =>
              complete(ToResponseMarshallable(lar))
            case Left(errors) =>
              completeWithParsingErrors(errors)
          }
        }
      } ~
        timedOptions { _ =>
          complete("OPTIONS")
        }
    }

  //lar/validate
  val validateLarRoute =
    path("validate") {
      parameters('check.as[String] ? "all") { checkType =>
        timedPost { _ =>
          entity(as[LarValidateRequest]) { req =>
            LarCsvParser(req.lar) match {
              case Right(lar) => validate(lar, checkType)
              case Left(errors) =>
                completeWithParsingErrors(errors)
            }
          }
        }
      }
    }

  private def completeWithParsingErrors(
      errors: List[ParserErrorModel.ParserValidationError]): Route = {
    val errorList = errors.map(e => e.errorMessage)
    complete(
      ToResponseMarshallable(
        StatusCodes.BadRequest -> LarValidateResponse(errorList)))
  }

  private def validate(lar: LoanApplicationRegister,
                       checkType: String): Route = {
    val validation: HmdaValidation[LoanApplicationRegister] = checkType match {
      case "all"         => checkAll(lar, lar.loan.ULI)
      case "syntactical" => checkSyntactical(lar, lar.loan.ULI)
      case "validity"    => checkValidity(lar, lar.loan.ULI)
      case "quality"     => checkQuality(lar, lar.loan.ULI)
    }

    val maybeErrors = validation.leftMap(xs => xs.toList).toEither

    maybeErrors match {
      case Right(l) => complete(l)
      case Left(errors) =>
        complete(ToResponseMarshallable(aggregateLarErrors(errors)))
    }
  }

  private def aggregateLarErrors(
      errors: List[ValidationError]): SingleValidationErrorResult = {
    val groupedErrors = errors.groupBy(_.validationErrorType)
    def allOfType(errorType: ValidationErrorType): Seq[String] = {
      groupedErrors.getOrElse(errorType, List()).map(e => e.editName)
    }

    SingleValidationErrorResult(
      ValidationErrorSummary(allOfType(Syntactical)),
      ValidationErrorSummary(allOfType(Validity)),
      ValidationErrorSummary(allOfType(Quality))
    )

  }

  def larRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("lar") {
            parseLarRoute ~ validateLarRoute
          }
        }
      }
    }
  }

}
