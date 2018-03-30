package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.model.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import hmda.parser.filing.ts.TsCsvParser
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.public.{TsValidateRequest, TsValidateResponse}
import hmda.api.http.codec.TsCodec._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

trait TsValidationHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  //ts/parse
  val parseTsRoute =
    path("parse") {
      timedPost { uri =>
        entity(as[TsValidateRequest]) { req =>
          TsCsvParser(req.ts) match {
            case Right(ts) => complete(ToResponseMarshallable(ts))
            case Left(errors) =>
              val errorList = errors.map(e => e.errorMessage)
              complete(
                ToResponseMarshallable(
                  StatusCodes.BadRequest -> TsValidateResponse(errorList)))
          }
        }
      }
    }

  def tsRoutes: Route = {
    encodeResponse {
      pathPrefix("ts") {
        parseTsRoute
      }
    }
  }

}
