package hmda.api.http.public

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpCharsets, HttpEntity, StatusCodes }
import akka.http.scaladsl.model.MediaTypes.`text/csv`
import akka.pattern.ask
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.{ ByteString, Timeout }
import hmda.api.model.public.RateSpreadModel.RateSpreadResponse
import hmda.api.protocol.apor.RateSpreadProtocol._
import hmda.api.util.FlowUtils
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait RateSpreadHttpApi extends HmdaCustomDirectives with ApiErrorProtocol with FlowUtils {

  implicit def timeout: Timeout

  def rateSpreadRoutes(supervisor: ActorRef) = {
    path("rateSpread") {
      extractExecutionContext { executor =>
        implicit val ec = executor
        encodeResponse {
          timedPost { _ =>
            entity(as[CalculateRateSpread]) { calculateRateSpread =>
              val fRateSpread = calculateSpread(supervisor, calculateRateSpread)
              onComplete(fRateSpread) {
                case Success(rateSpread) =>
                  complete(ToResponseMarshallable(RateSpreadResponse(rateSpread)))
                case Failure(error) =>
                  log.error(error.getLocalizedMessage)
                  complete(ToResponseMarshallable(StatusCodes.InternalServerError))
              }
            }
          }
        }
      }
    } ~
      path("rateSpread" / "csv") {
        timedPost { _ =>
          fileUpload("file") {
            case (_, byteSource) =>
              val headerSource = Source
                .fromIterator(() => List(
                  "action_taken_type,",
                  "amortization_type,",
                  "rate_type,",
                  "apr,",
                  "lockin_date,",
                  "reverse_mortgage\n"
                ).toIterator)

              val rateSpread = processRateSpreadFile(supervisor, byteSource)
              val csv = headerSource.map(s => ByteString(s)).concat(rateSpread)
              complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))
            case _ =>
              complete(ToResponseMarshallable(StatusCodes.BadRequest))
          }
        }
      }
  }

  private def processRateSpreadFile(supervisor: ActorRef, byteSource: Source[ByteString, Any]) = {
    val fHmdaAporPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
    byteSource
      .via(framing)
      .map(_.utf8String)
      .map(s => (s, CalculateRateSpread(s)))
      .mapAsync(parallelism) { x =>
        val y = (Future(x._1), calculateSpread(supervisor, x._2))
        for (a <- y._1; b <- y._2) yield (a, b)
      }
      .map(x => s"${x._1},${x._2}\n")
      .map(s => ByteString(s))
  }

  private def calculateSpread(supervisor: ActorRef, calculateRateSpread: CalculateRateSpread): Future[String] = {
    val fHmdaAporPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
    for {
      aporPersistence <- fHmdaAporPersistence
      spread <- (aporPersistence ? calculateRateSpread).mapTo[Option[Double]]
    } yield spread.map(x => x.toString).getOrElse("NA")
  }
}
