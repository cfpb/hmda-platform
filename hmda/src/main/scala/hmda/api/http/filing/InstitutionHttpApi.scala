package hmda.api.http.filing

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.api.http.directives.{ HmdaTimeDirectives, QuarterlyFilingAuthorization }
import hmda.util.http.FilingResponseUtils._
import hmda.messages.institution.InstitutionCommands.GetInstitutionDetails
import hmda.model.institution.InstitutionDetail
import hmda.api.http.PathMatchers._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.auth.OAuth2Authorization
import hmda.persistence.institution.InstitutionPersistence.selectInstitution

trait InstitutionHttpApi extends HmdaTimeDirectives with QuarterlyFilingAuthorization {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  // GET /institutions/<lei>/year/<y>
  // GET /institutions/<lei>/year/<y>/quarter/<q>
  def institutionReadPath(oAuth2Authorization: OAuth2Authorization): Route =
    //TODO: add rules
    pathPrefix("institutions" / Segment / "year" / IntNumber) { (lei, year) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        timedGet { uri =>
          pathEndOrSingleSlash {
            obtainFilingDetails(lei, year, None, uri)
          } ~ path("quarter" / Quarter) { quarter =>
            quarterlyFilingAllowed(lei, year) {
              obtainFilingDetails(lei, year, Option(quarter), uri)
            }
          }
        }
      }
    }

  def obtainFilingDetails(lei: String, year: Int, quarter: Option[String], uri: Uri): Route = {
    val institutionPersistence                      = selectInstitution(sharding, lei, year)
    val iDetails: Future[Option[InstitutionDetail]] = institutionPersistence ? (ref => GetInstitutionDetails(ref))
    onComplete(iDetails) {
      case Success(Some(institutionDetails)) =>
        complete(ToResponseMarshallable(institutionDetails))
      case Success(None) =>
        val errorResponse =
          ErrorResponse(404, s"Institution: $lei does not exist", uri.path)
        complete(
          ToResponseMarshallable(StatusCodes.NotFound -> errorResponse)
        )
      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
  }

  def institutionRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionReadPath(oAuth2Authorization)
        }
      }
    }
}
