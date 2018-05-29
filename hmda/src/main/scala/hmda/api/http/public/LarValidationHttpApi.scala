package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.api.http.model.public.{LarValidateRequest, LarValidateResponse}
import hmda.parser.filing.lar.LarCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.codec.filing.LarCodec._
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

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
              val errorList = errors.map(e => e.errorMessage)
              complete(
                ToResponseMarshallable(
                  StatusCodes.BadRequest -> LarValidateResponse(errorList)))
          }
        }
      }
    }

  def larRoutes: Route = {
    cors() {
      encodeResponse {
        pathPrefix("lar") {
          parseLarRoute
        }
      }
    }
  }

}
