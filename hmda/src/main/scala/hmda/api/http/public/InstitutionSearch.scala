package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.query.ts._
import hmda.query.DbConfiguration._
import hmda.model.institution.{HmdaFiler, HmdaFilerResponse}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait FilersHttpApi extends TsComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  def tsRepository = new TransmittalSheetRepository(dbConfig)

  val filerListRoute: Route = {
    path("filers" / Segment) { (year) =>
      get {

        val filerSet = for {
          ts <- tsRepository.getAllSheets()
        } yield {
          ts.map(tsEntity =>
              HmdaFiler(tsEntity.lei, tsEntity.name, tsEntity.year.toString))
            .toSet
        }

        onComplete(filerSet) {
          case Success(filerSet) =>
            complete(ToResponseMarshallable(HmdaFilerResponse(filerSet)))
          case Failure(error) =>
            complete(
              ToResponseMarshallable(
                StatusCodes.BadRequest -> error.getLocalizedMessage))
        }

      }
    }
  }

  def hmdaFilerRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("lar") {
            filerListRoute
          }
        }
      }
    }
  }
}
