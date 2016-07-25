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
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.util.Timeout
import hmda.api.processing.lar.SingleLarValidation.{ CheckAll, CheckQuality, CheckSyntactical, CheckValidity }
import hmda.api.protocol.fi.lar.LarProtocol
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.engine.ValidationError
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait LarHttpApi extends LarProtocol with ValidationResultProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  val parseLarRoute =
    pathPrefix("lar") {
      path("parse") {
        post {
          entity(as[String]) { s =>
            val lar = LarCsvParser(s)
            //TODO: return human readable errors when parser fails. See issue #62
            complete {
              ToResponseMarshallable(lar)
            }
          }
        }
      }
    }

  val validateLarRoute =
    pathPrefix("lar") {
      path("validate") {
        parameters('check.as[String] ? "all") { (checkType) =>
          post {
            entity(as[LoanApplicationRegister]) { lar =>
              val larValidation = system.actorSelection("/user/larValidation")
              val checkMessage = checkType match {
                case "syntactical" => CheckSyntactical(lar, None)
                case "validity" => CheckValidity(lar, None)
                case "quality" => CheckQuality(lar, None)
                case _ => CheckAll(lar, None)
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

  val larRoutes = parseLarRoute ~ validateLarRoute

}
