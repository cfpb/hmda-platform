package hmda.api.http.public

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import hmda.api.model.public.RateSpreadModel.RateSpreadResponse
import hmda.api.protocol.apor.RateSpreadProtocol._
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread

import scala.util.{ Failure, Success }

trait RateSpreadHttpApi extends HmdaCustomDirectives with ApiErrorProtocol {

  implicit def timeout: Timeout

  def rateSpreadRoutes(supervisor: ActorRef) =
    path("rateSpread") {
      extractExecutionContext { executor =>
        implicit val ec = executor
        encodeResponse {
          entity(as[CalculateRateSpread]) { calculateRateSpread =>
            val fHmdaAporPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
            val fRateSpread = for {
              aporPersistence <- fHmdaAporPersistence
              spread <- (aporPersistence ? calculateRateSpread).mapTo[Option[Double]]
            } yield spread

            onComplete(fRateSpread) {
              case Success(maybeRateSpread) =>
                maybeRateSpread match {
                  case Some(rateSpread) =>
                    complete(ToResponseMarshallable(RateSpreadResponse(rateSpread.toString)))
                  case None =>
                    complete(ToResponseMarshallable(RateSpreadResponse("NA")))
                }

              case Failure(error) =>
                log.error(error.getLocalizedMessage)
                complete(ToResponseMarshallable(StatusCodes.InternalServerError))
            }
          }
        }
      }
    }

}
