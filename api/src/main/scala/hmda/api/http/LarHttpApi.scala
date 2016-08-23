package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
<<<<<<< HEAD
import akka.http.javadsl.server.directives.RouteDirectives
import akka.http.scaladsl
=======
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
>>>>>>> master
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ ContentTypes, _ }
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
<<<<<<< HEAD
import hmda.parser.fi.lar.LarCsvParser
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ ContentTypes, _ }
import akka.http.scaladsl.server
import akka.http.scaladsl.server.StandardRoute
=======
import akka.stream.ActorMaterializer
>>>>>>> master
import akka.util.Timeout
import hmda.api.model.SingleValidationErrorResult
import hmda.api.protocol.fi.lar.LarProtocol
import hmda.api.protocol.validation.ValidationResultProtocol
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.processing.SingleLarValidation.{ CheckAll, CheckQuality, CheckSyntactical, CheckValidity }
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import spray.json._

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
        parameters('check.as[String] ? "all") { (checkType) =>
          timedPost {
            entity(as[LoanApplicationRegister]) { lar =>
<<<<<<< HEAD
              validateRoute(lar, checkType)
            }
          }
        }
      }
    }

  val parseAndValidateLarRoute =
    pathPrefix("lar") {
      path("parseAndValidate") {
        parameters('check.as[String] ? "all") { (checkType) =>
          timedPost {
            entity(as[String]) { s =>
              LarCsvParser(s) match {
                case Right(lar) => validateRoute(lar, checkType)
                case Left(errors) => complete(errorsAsResponse(errors))
=======
              val larValidation = system.actorSelection("/user/larValidation")
              val checkMessage = checkType match {
                case "syntactical" => CheckSyntactical(lar, ValidationContext(None))
                case "validity" => CheckValidity(lar, ValidationContext(None))
                case "quality" => CheckQuality(lar, ValidationContext(None))
                case _ => CheckAll(lar, ValidationContext(None))
              }
              onComplete((larValidation ? checkMessage).mapTo[ValidationErrors]) {
                case Success(xs) =>
                  complete(ToResponseMarshallable(aggregateErrors(xs)))
                case Failure(e) =>
                  complete(HttpResponse(StatusCodes.InternalServerError))
>>>>>>> master
              }
            }
          }
        }
      }
    }

<<<<<<< HEAD
  def validateRoute(lar: LoanApplicationRegister, checkType: String): scaladsl.server.Route = {
    val larValidation = system.actorSelection("/user/larValidation")
    val checkMessage = checkType match {
      case "syntactical" => CheckSyntactical(lar, ValidationContext(None))
      case "validity" => CheckValidity(lar, ValidationContext(None))
      case "quality" => CheckQuality(lar, ValidationContext(None))
      case _ => CheckAll(lar, ValidationContext(None))
    }
    onComplete((larValidation ? checkMessage).mapTo[List[ValidationError]]) {
      case Success(xs) =>
        complete(ToResponseMarshallable(xs))
      case Failure(e) =>
        complete(HttpResponse(StatusCodes.InternalServerError))
    }
=======
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

>>>>>>> master
  }

  def errorsAsResponse(list: List[String]): HttpResponse = {
    val errorEntity = HttpEntity(ContentTypes.`application/json`, list.toJson.toString)
    HttpResponse(StatusCodes.BadRequest, entity = errorEntity)
  }

  val larRoutes = parseLarRoute ~ validateLarRoute ~ parseAndValidateLarRoute

}
