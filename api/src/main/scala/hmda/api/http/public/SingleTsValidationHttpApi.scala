package hmda.api.http.public

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.SingleValidationErrorResult
import hmda.api.protocol.fi.ts.TsProtocol
import hmda.api.protocol.processing.ParserResultsProtocol
import hmda.api.protocol.validation.ValidationResultProtocol
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.validation.{ Quality, Syntactical, ValidationErrorType, Validity }
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SingleTsValidation
import hmda.persistence.processing.SingleTsValidation.{ CheckAll, CheckQuality, CheckSyntactical, CheckValidity }
import hmda.validation.context.ValidationContext
import hmda.validation.engine._

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait SingleTsValidationHttpApi extends TsProtocol with ValidationResultProtocol with HmdaCustomDirectives with ParserResultsProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  // ts/parse
  val parseTsRoute =
    path("parse") {
      timedPost { _ =>
        entity(as[String]) { s =>
          TsCsvParser(s) match {
            case Right(ts) => complete(ToResponseMarshallable(ts))
            case Left(errors) => complete(ToResponseMarshallable(StatusCodes.BadRequest -> errors))
          }
        }
      }
    }

  // ts/validate
  def validateTsRoute(supervisor: ActorRef) =
    path("validate") {
      parameters('check.as[String] ? "all") { (checkType) =>
        timedPost { uri =>
          entity(as[TransmittalSheet]) { ts =>
            validateRoute(supervisor, ts, checkType, uri)
          }
        }
      }
    }

  // ts/parseAndValidate
  def parseAndValidateTsRoute(supervisor: ActorRef) =
    path("parseAndValidate") {
      parameters('check.as[String] ? "all") { (checkType) =>
        timedPost { uri =>
          entity(as[String]) { s =>
            TsCsvParser(s) match {
              case Right(ts) => validateRoute(supervisor, ts, checkType, uri)
              case Left(errors) => complete(ToResponseMarshallable(StatusCodes.BadRequest -> errors))
            }
          }
        }
      }
    }

  def validateRoute(supervisor: ActorRef, ts: TransmittalSheet, checkType: String, uri: Uri) = {
    val ftsValidation = (supervisor ? FindActorByName(SingleTsValidation.name)).mapTo[ActorRef]
    val vContext = ValidationContext(None, None)
    val checkMessage = checkType match {
      case "syntactical" => CheckSyntactical(ts, vContext)
      case "validity" => CheckValidity(ts, vContext)
      case "quality" => CheckQuality(ts, vContext)
      case _ => CheckAll(ts, vContext)
    }
    val fValidationErrors = for {
      tsValidation <- ftsValidation
      ve <- (tsValidation ? checkMessage).mapTo[ValidationErrors]
    } yield ve

    onComplete(fValidationErrors) {
      case Success(validationErrors) =>
        complete(ToResponseMarshallable(aggregateTsErrors(validationErrors)))
      case Failure(error) =>
        completeWithInternalError(uri, error)

    }
  }

  def aggregateTsErrors(validationErrors: ValidationErrors): SingleValidationErrorResult = {
    val errors = validationErrors.errors.groupBy(_.errorType)
    def allOfType(errorType: ValidationErrorType): Seq[String] = {
      errors.getOrElse(errorType, List()).map(e => e.ruleName)
    }

    SingleValidationErrorResult(
      ValidationErrorsSummary(allOfType(Syntactical)),
      ValidationErrorsSummary(allOfType(Validity)),
      ValidationErrorsSummary(allOfType(Quality))
    )
  }

  def tsRoutes(supervisor: ActorRef) =
    encodeResponse {
      pathPrefix("ts") {
        parseTsRoute ~ validateTsRoute(supervisor) ~ parseAndValidateTsRoute(supervisor)
      }
    }

}