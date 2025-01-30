package hmda.api.http.public

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.PathMatchers._
import hmda.api.http.model.public.LarValidateRequest
import hmda.api.http.public.FilingValidationHttpDirectives._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.validation.LarValidationError
import hmda.parser.filing.lar.LarCsvParser
import hmda.utils.YearUtils.Period
import hmda.validation.HmdaValidation
import hmda.validation.context.ValidationContext
import hmda.validation.engine._

object LarValidationHttpApi {
  def create: Route = new LarValidationHttpApi().larRoutes
}

private class LarValidationHttpApi {
  //lar/parse
  private val parseLarRoute =
    path("parse") {
      post {
        respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
          entity(as[LarValidateRequest]) { req =>
            LarCsvParser(req.lar) match {
              case Right(lar) =>
                complete(ToResponseMarshallable(lar))
              case Left(errors) =>
                completeWithParsingErrors(Some(req.lar), errors)
            }
          }
        }
      } ~
        options(complete("OPTIONS"))
    }

  //lar/validate/<year>
  private val validateYearLarRoute =
    path("validate" / IntNumber) { year =>
      parameters('check.as[String] ? "all") { checkType =>
        post {
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            entity(as[LarValidateRequest]) { req =>
              LarCsvParser(req.lar) match {
                case Right(lar) => validate(lar, checkType, year, None)
                case Left(errors) =>
                  completeWithParsingErrors(Some(req.lar), errors)
              }
            }
          }
        }
      }
    }

  //lar/validate/<year>
  private val validateQuarterLarRoute =
    path("validate" / IntNumber / "quarter" / Quarter) { (year, quarter) =>
      parameters('check.as[String] ? "all") { checkType =>
        post {
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            entity(as[LarValidateRequest]) { req =>
              LarCsvParser(req.lar) match {
                case Right(lar) => validate(lar, checkType, year, Some(quarter))
                case Left(errors) =>
                  completeWithParsingErrors(Some(req.lar), errors)
              }
            }
          }
        }
      }
    }

  private def validate(lar: LoanApplicationRegister, checkType: String, year: Int, quarter: Option[String]): Route = {
    val ctx              = ValidationContext(filingPeriod = Some(Period(year, quarter)))
    val validationEngine = selectLarEngine(year, None)
    import validationEngine._
    val validation: HmdaValidation[LoanApplicationRegister] = checkType match {
      case "all" => checkAll(lar, lar.loan.ULI, ctx, LarValidationError)
      case "syntactical" =>
        checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)
      case "validity" => checkValidity(lar, lar.loan.ULI, ctx, LarValidationError)
      case "quality"  => checkQuality(lar, lar.loan.ULI, ctx)
    }

    val maybeErrors = validation.leftMap(xs => xs.toList).toEither

    maybeErrors match {
      case Right(l) => complete(l)
      case Left(errors) =>
        complete(aggregateErrors(errors, Period(year, quarter)))
    }
  }

  def larRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("lar") {
            parseLarRoute ~ validateYearLarRoute ~ validateQuarterLarRoute
          }
        }
      }
    }
}