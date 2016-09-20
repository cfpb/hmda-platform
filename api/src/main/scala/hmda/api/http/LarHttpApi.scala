package hmda.api.http

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ ContentTypes, _ }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ ContentTypes, _ }
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import hmda.api.model.{ ErrorResponse, SingleValidationErrorResult }
import hmda.api.protocol.fi.lar.LarProtocol
import hmda.api.protocol.validation.ValidationResultProtocol
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.processing.SingleLarValidation.{ CheckAll, CheckQuality, CheckSyntactical, CheckValidity }
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import spray.json._
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.processing.SingleLarValidation

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait LarHttpApi extends LarProtocol with ValidationResultProtocol with HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  val parseLarRoute =
    pathPrefix("lar") {
      path("parse") {
        timedPost {
          entity(as[String]) { s =>
            LarCsvParser(s) match {
              case Right(lar) => complete(ToResponseMarshallable(lar))
              case Left(errors) => complete(errorsAsResponse(errors))
            }
          }
        }
      }
    }

  val validateLarRoute =
    pathPrefix("lar") {
      path("validate") {
        val path = "lar/validate"
        parameters('check.as[String] ? "all") { (checkType) =>
          timedPost {
            entity(as[LoanApplicationRegister]) { lar =>
              validateRoute(lar, checkType, path)
            }
          }
        }
      }
    }

  val parseAndValidateLarRoute =
    pathPrefix("lar") {
      path("parseAndValidate") {
        val path = "lar/parseAndValidate"
        parameters('check.as[String] ? "all") { (checkType) =>
          timedPost {
            entity(as[String]) { s =>
              LarCsvParser(s) match {
                case Right(lar) => validateRoute(lar, checkType, path)
                case Left(errors) => complete(errorsAsResponse(errors))
              }
            }
          }
        }
      }
    }

  def validateRoute(lar: LoanApplicationRegister, checkType: String, path: String) = {
    val supervisor = system.actorSelection("/user/supervisor")
    val fLarValidation = (supervisor ? FindActorByName(SingleLarValidation.name)).mapTo[ActorRef]
    val vContext = ValidationContext(None, None)
    val checkMessage = checkType match {
      case "syntactical" => CheckSyntactical(lar, vContext)
      case "validity" => CheckValidity(lar, vContext)
      case "quality" => CheckQuality(lar, vContext)
      case _ => CheckAll(lar, vContext)
    }
    val validationErrors = for {
      larValidation <- fLarValidation
      ve <- (larValidation ? checkMessage).mapTo[ValidationErrors]
    } yield ve

    onComplete(validationErrors) {
      case Success(validationErrors) =>
        complete(ToResponseMarshallable(aggregateErrors(validationErrors)))
      case Failure(error) =>
        completeWithInternalError("lar/parseAndValidate", error)

    }
  }

  def aggregateErrors(validationErrors: ValidationErrors): SingleValidationErrorResult = {
    val errors = validationErrors.errors.groupBy(_.errorType)
    def allOfType(errorType: ValidationErrorType): Seq[String] = {
      errors.getOrElse(errorType, List()).map(e => e.name)
    }

    SingleValidationErrorResult(
      ValidationErrorsSummary(allOfType(Syntactical)),
      ValidationErrorsSummary(allOfType(Validity)),
      ValidationErrorsSummary(allOfType(Quality))
    )
  }

  def errorsAsResponse(list: List[String]): HttpResponse = {
    val errorEntity = HttpEntity(ContentTypes.`application/json`, list.toJson.toString)
    HttpResponse(StatusCodes.BadRequest, entity = errorEntity)
  }

  val larRoutes = parseLarRoute ~ validateLarRoute ~ parseAndValidateLarRoute

}
