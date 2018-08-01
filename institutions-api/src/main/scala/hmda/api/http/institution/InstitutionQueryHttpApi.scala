package hmda.api.http.institution

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.query.DbConfiguration._
import hmda.query.institution.InstitutionComponent
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.api.http.model.ErrorResponse
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait InstitutionQueryHttpApi
    extends HmdaTimeDirectives
    with InstitutionComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  val repository = new InstitutionRepository(config)

  val institutionByIdPath =
    path("institutions" / Segment) { lei =>
      timedGet { uri =>
        val fInstitution = repository.findById(lei)
        onComplete(fInstitution) {
          case Success(Some(institution)) => complete("Institution")
          case Success(None) =>
            complete(ToResponseMarshallable(HttpResponse(StatusCodes.NotFound)))
          case Failure(error) =>
            val errorResponse =
              ErrorResponse(500, error.getLocalizedMessage, uri.path)
            complete(
              ToResponseMarshallable(
                StatusCodes.InternalServerError -> errorResponse))
        }
      }
    }

  val institutionByDomainPath =
    path("institutions") {
      complete("OK")
    }

  def institutionPublicRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionByIdPath ~ institutionByDomainPath
        }
      }
    }

}
