package hmda.api.http.public

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.PathMatchers._
import hmda.api.http.model.public.TsValidateRequest
import hmda.api.http.public.FilingValidationHttpDirectives._
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.validation.TsValidationError
import hmda.parser.filing.ts.TsCsvParser
import hmda.utils.YearUtils.Period
import hmda.validation.HmdaValidation
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import io.circe.generic.auto._

object TsValidationHttpApi {
  def create: Route = new TsValidationHttpApi().tsRoutes
}
private class TsValidationHttpApi {
  //ts/parse
  private val parseTsRoute =
    path("parse") {
      (extractUri & post) { _ =>
        respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
          entity(as[TsValidateRequest]) { req =>
            TsCsvParser(req.ts) match {
              case Right(ts) => complete(ToResponseMarshallable(ts))
              case Left(errors) =>
                completeWithParsingErrors(None, errors)
            }
          }
        }
      } ~
        (extractUri & options)(_ => complete("OPTIONS"))
    }

  //ts/validate/<year>
  private val validateYearTsRoute =
    path("validate" / IntNumber) { year =>
      parameters('check.as[String] ? "all") { checkType =>
        post {
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            entity(as[TsValidateRequest]) { req =>
              TsCsvParser(req.ts) match {
                case Right(ts) =>
                  validate(ts, checkType, year, None)
                case Left(errors) =>
                  completeWithParsingErrors(None, errors)
              }
            }
          }
        }
      }
    }

  //ts/validate/<year>/quarter/<period>
  // $COVERAGE-OFF$
  private val validateQuarterTsRoute =
  path("validate" / IntNumber / "quarter" / Quarter) { (year, quarter) =>
    parameters('check.as[String] ? "all") { checkType =>
      post {
        respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
          entity(as[TsValidateRequest]) { req =>
            TsCsvParser(req.ts) match {
              case Right(ts) =>
                validate(ts, checkType, year, Some(quarter))
              case Left(errors) =>
                completeWithParsingErrors(None, errors)
            }
          }
        }
      }
    }
  }
  // $COVERAGE-ON$

  private def validate(ts: TransmittalSheet, checkType: String, year: Int, quarter: Option[String]): Route = {
    val ctx              = ValidationContext(filingPeriod = Some(Period(year, quarter)))
    val period           = ctx.filingPeriod.get
    val validationEngine = selectTsEngine(period.year, period.quarter)
    import validationEngine._
    val validation: HmdaValidation[TransmittalSheet] = checkType match {
      case "all" =>
        checkAll(ts, ts.LEI, ctx, TsValidationError)

      case "syntactical" =>
        checkSyntactical(ts, ts.LEI, ctx, TsValidationError)

      case "validity" =>
        checkValidity(ts, ts.LEI, ctx, TsValidationError)
    }

    val maybeErrors = validation.leftMap(xs => xs.toList).toEither

    maybeErrors match {
      case Right(t) =>
        complete(t)

      case Left(errors) =>
        complete(ToResponseMarshallable(aggregateErrors(errors, Period(year, quarter))))
    }
  }

  def tsRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("ts") {
            parseTsRoute ~ validateYearTsRoute ~ validateQuarterTsRoute
          }
        }
      }
    }

}