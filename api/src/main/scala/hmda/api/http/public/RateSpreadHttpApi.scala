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

  def rateSpreadRoutes(supervisor: ActorRef) = individualRateSpread(supervisor) ~ batchRateSpread(supervisor)

  def individualRateSpread(supervisor: ActorRef) =
    path("rateSpread") {
      encodeResponse {
        timedPost { _ =>
          entity(as[CalculateRateSpread]) { request =>
            val fHmdaAporPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
            val fRateSpread = calculateSpread(fHmdaAporPersistence, request)
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

  def batchRateSpread(supervisor: ActorRef) =
    path("rateSpread" / "csv") {
      timedPost { _ =>
        fileUpload("file") {
          case (_, byteSource) =>
            val fHmdaAporPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
            val rateSpread = processRateSpreadFile(fHmdaAporPersistence, byteSource)
            val csv = headerSource.map(s => ByteString(s)).concat(rateSpread)
            complete(HttpEntity.Chunked.fromData(`text/csv`.toContentType(HttpCharsets.`UTF-8`), csv))
          case _ =>
            complete(ToResponseMarshallable(StatusCodes.BadRequest))
        }
      }
    }

  private val headerSource = Source.fromIterator(() => List(
    "action_taken_type,", "loan_term,", "amortization_type,",
    "apr,", "lock_in_date,", "reverse_mortgage,", "rate_spread\n"
  ).toIterator)

  private def processRateSpreadFile(fAporActor: Future[ActorRef], byteSource: Source[ByteString, Any]) = {
    byteSource
      .via(framing)
      .map(_.utf8String)
      .mapAsync(parallelism)(line => csvResultLine(fAporActor, line))
      .map(s => ByteString(s))
  }

  def csvResultLine(fAporActor: Future[ActorRef], line: String): Future[String] = {
    CalculateRateSpread.fromCsv(line) match {
      case Some(command) =>
        val rateSpreadF = calculateSpread(fAporActor, command)
        val resultValueF = rateSpreadF.map {
          case Right(result) => result.rateSpread
          case Left(error) => s"error: ${error.message}"
        }
        resultValueF.map(value => s"$line,$value\n")
      case None => Future(s"$line,error: invalid rate spread CSV")
    }
  }

  private def calculateSpread(fAporActor: Future[ActorRef], calculateRateSpread: CalculateRateSpread): Future[Either[RateSpreadError, RateSpreadResponse]] = {
    for {
      a <- fAporActor
      r <- (a ? calculateRateSpread).mapTo[Either[RateSpreadError, RateSpreadResponse]]
    } yield r
  }
}
