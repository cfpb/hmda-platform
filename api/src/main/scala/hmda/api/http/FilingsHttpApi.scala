package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.util.Timeout
import akka.pattern.ask
import hmda.api.model.Filings
import hmda.api.persistence.CommonMessages._
import hmda.api.persistence.FilingPersistence
import hmda.api.persistence.FilingPersistence.GetFilingByPeriod
import hmda.api.protocol.processing.FilingProtocol
import hmda.model.fi.Filing

import scala.util.{ Failure, Success }

trait FilingsHttpApi extends FilingProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val filingsPath =
    path("institutions" / Segment / "filings") { fid =>
      val filingsActor = system.actorOf(FilingPersistence.props(fid))
      get {
        val fFilings = (filingsActor ? GetState).mapTo[Seq[Filing]]
        onComplete(fFilings) {
          case Success(filings) =>
            filingsActor ! Shutdown
            complete(ToResponseMarshallable(Filings(filings)))
          case Failure(error) =>
            filingsActor ! Shutdown
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val filingByPeriod =
    path("institutions" / Segment / "filings" / Segment) { (fid, period) =>
      val filingsActor = system.actorOf(FilingPersistence.props(fid))
      get {
        val fFiling = (filingsActor ? GetFilingByPeriod(period)).mapTo[Filing]
        onComplete(fFiling) {
          case Success(filing) =>
            filingsActor ! Shutdown
            complete(ToResponseMarshallable(filing))
          case Failure(error) =>
            filingsActor ! Shutdown
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val filingsRoutes = filingsPath ~ filingByPeriod

}
