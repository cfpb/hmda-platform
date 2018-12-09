package hmda.census.api.http

import akka.actor.ActorSystem
import hmda.census.query.CensusComponent
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.ErrorResponse
import hmda.census.api.http.model.CensusResponse
//import hmda.institution.query.{InstitutionComponent, InstitutionEntity}
import hmda.query.DbConfiguration._
import hmda.api.http.codec.institution.InstitutionCodec._
//import hmda.institution.api.http.model.InstitutionsResponse
import hmda.model.institution.Institution
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext

trait CensusQueryHttpApi extends HmdaTimeDirectives with CensusComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  implicit val censusRepository = new CensusRepository(dbConfig)

  val censusByIdPath =
    path("census" / IntNumber) { year =>
      timedGet { uri =>
        val f = findByCollectionYr(year)
        onComplete(f) {
          case Success(censuses) =>
            complete(ToResponseMarshallable(CensusResponse(censuses)))
        }
      }
    }

  def censusRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          censusByIdPath
        }
      }
    }

}
