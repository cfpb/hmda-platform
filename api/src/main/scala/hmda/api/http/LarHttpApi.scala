package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import hmda.api.protocol.validation.ValidationResultProtocol
import akka.pattern.ask
import hmda.parser.fi.lar.LarCsvParser
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ ContentTypes, _ }
import akka.util.Timeout
import hmda.api.processing.lar.SingleLarValidation.{ CheckAll, CheckQuality, CheckSyntactical, CheckValidity }
import hmda.api.protocol.fi.lar.LarProtocol
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.engine.ValidationError

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }
import spray.json._

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
            }
          }
        }
      }
    }

  def errorsAsResponse(list: List[String]): HttpResponse = {
    val errorEntity = HttpEntity(ContentTypes.`application/json`, list.toJson.toString)
    HttpResponse(StatusCodes.BadRequest, entity = errorEntity)
  }

  val larRoutes = //hmdaAuthorize {
    parseLarRoute ~ validateLarRoute
  //} ~ unauthorizedAccess

}
