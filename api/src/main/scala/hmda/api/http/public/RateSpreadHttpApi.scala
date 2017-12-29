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
import akka.stream.scaladsl.Source
import akka.util.{ ByteString, Timeout }
import hmda.api.protocol.apor.RateSpreadProtocol._
import hmda.api.util.FlowUtils
import hmda.model.rateSpread.{ RateSpreadError, RateSpreadResponse }
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait RateSpreadHttpApi extends HmdaCustomDirectives with ApiErrorProtocol with FlowUtils {

  implicit def timeout: Timeout

  def individualRateSpread(implicit supervisor: ActorRef) =
    path("rateSpread") {
      encodeResponse {
        timedPost { _ =>
          entity(as[CalculateRateSpread]) { request =>
            val fRateSpread = calculateSpread(supervisor, request)
            onComplete(fRateSpread) {
              case Success(Right(response)) => complete(ToResponseMarshallable(response))
              case Success(Left(errorResponse)) =>
                val errorCode = StatusCodes.getForKey(errorResponse.code).getOrElse(StatusCodes.NotFound)
                complete(ToResponseMarshallable(errorCode -> errorResponse))
              case Failure(error) =>
                log.error(error.getLocalizedMessage)
                complete(ToResponseMarshallable(StatusCodes.InternalServerError))
            }
          }
        }
      }
    }

  /*
  def batchRateSpread(implicit supervisor: ActorRef) =
    path("rateSpread" / "csv") {
      timedPost { _ =>
        fileUpload("file") {
          /*
            case (_, byteSource) =>
              val headerSource = Source
                .fromIterator(() => List(
                  "action_taken_type,",
                  "loan_term,",
                  "amortization_type,",
                  "apr,",
                  "lock_in_date,",
                  "reverse_mortgage," +
                    "rate_spread\n"
                ).toIterator)

              val rateSpread = processRateSpreadFile(supervisor, byteSource)
              val csv = headerSource.map(s => ByteString(s)).concat(rateSpread)
              complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))
              */
          case _ =>
            complete(ToResponseMarshallable(StatusCodes.BadRequest))
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
  */

  def rateSpreadRoutes(supervisor: ActorRef) = {
    implicit val sv: ActorRef = supervisor
    individualRateSpread //~ batchRateSpread
  }

  private def calculateSpread(supervisor: ActorRef, calculateRateSpread: CalculateRateSpread): Future[Either[RateSpreadError, RateSpreadResponse]] = {
    val fHmdaAporPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
    fHmdaAporPersistence.map(a => a ? calculateRateSpread).mapTo[Either[RateSpreadError, RateSpreadResponse]]
  }
}
