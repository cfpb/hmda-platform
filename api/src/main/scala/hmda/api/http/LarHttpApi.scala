package hmda.api.http

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import hmda.api.model.SingleValidationErrorResult
import hmda.api.protocol.fi.lar.LarProtocol
import hmda.api.protocol.processing.ParserResultsProtocol
import hmda.api.protocol.validation.ValidationResultProtocol
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SingleLarValidation
import hmda.persistence.processing.SingleLarValidation.{ CheckAll, CheckQuality, CheckSyntactical, CheckValidity }
import hmda.validation.context.ValidationContext
import hmda.validation.engine._

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait LarHttpApi extends LarProtocol with ValidationResultProtocol with HmdaCustomDirectives with ParserResultsProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  // lar/parse
  val parseLarRoute =
    path("parse") {
      timedPost { _ =>
        entity(as[String]) { s =>
          LarCsvParser(s) match {
            case Right(lar) => complete(ToResponseMarshallable(lar))
            case Left(errors) => complete(ToResponseMarshallable(StatusCodes.BadRequest -> errors))
          }
        }
      }
    }

  // lar/validate
  val validateLarRoute =
    path("validate") {
      parameters('check.as[String] ? "all") { (checkType) =>
        timedPost { uri =>
          entity(as[LoanApplicationRegister]) { lar =>
            validateRoute(lar, checkType, uri)
          }
        }
      }
    }

  // lar/parseAndValidate
  val parseAndValidateLarRoute =
    path("parseAndValidate") {
      parameters('check.as[String] ? "all") { (checkType) =>
        timedPost { uri =>
          entity(as[String]) { s =>
            LarCsvParser(s) match {
              case Right(lar) => validateRoute(lar, checkType, uri)
              case Left(errors) => complete(ToResponseMarshallable(StatusCodes.BadRequest -> errors))
            }
          }
        }
      }
    }

  def validateRoute(lar: LoanApplicationRegister, checkType: String, uri: Uri) = {
    val supervisor = system.actorSelection("/user/supervisor")
    val fLarValidation = (supervisor ? FindActorByName(SingleLarValidation.name)).mapTo[ActorRef]
    val vContext = ValidationContext(None, None)
    val checkMessage = checkType match {
      case "syntactical" => CheckSyntactical(lar, vContext)
      case "validity" => CheckValidity(lar, vContext)
      case "quality" => CheckQuality(lar, vContext)
      case _ => CheckAll(lar, vContext)
    }
    val fValidationErrors = for {
      larValidation <- fLarValidation
      ve <- (larValidation ? checkMessage).mapTo[ValidationErrors]
    } yield ve

    onComplete(fValidationErrors) {
      case Success(validationErrors) =>
        complete(ToResponseMarshallable(aggregateErrors(validationErrors)))
      case Failure(error) =>
        completeWithInternalError(uri, error)

    }
  }

  def aggregateErrors(validationErrors: ValidationErrors): SingleValidationErrorResult = {
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

  val larRoutes =
    encodeResponse {
      pathPrefix("lar") {
        parseLarRoute ~ validateLarRoute ~ parseAndValidateLarRoute
      }
    }

}
