package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.RawHeader
import hmda.parser.filing.ts.TsCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.public.TsValidateRequest
import hmda.api.http.directives.HmdaTimeDirectives
import io.circe.generic.auto._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.validation.TsValidationError
import hmda.validation.HmdaValidation
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.utils.YearUtils.Period

import scala.concurrent.ExecutionContext
import hmda.api.http.PathMatchers._
import HmdaTimeDirectives._
import org.slf4j.Logger

trait TsValidationHttpApi extends FilingValidationHttpApi {

  implicit val system: ActorSystem
  implicit val materializer: Materializer
  val log: Logger
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  //ts/parse
  val parseTsRoute =
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
  val validateYearTsRoute =
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
  val validateQuarterTsRoute =
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
            timed(parseTsRoute ~ validateYearTsRoute ~ validateQuarterTsRoute)
          }
        }
      }
    }

}