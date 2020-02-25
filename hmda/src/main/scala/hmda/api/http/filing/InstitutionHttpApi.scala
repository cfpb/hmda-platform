package hmda.api.http.filing

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.api.http.directives.{HmdaTimeDirectives, QuarterlyFilingAuthorization}
import hmda.util.http.FilingResponseUtils._
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.institution.{Institution, InstitutionDetail}
import hmda.api.http.PathMatchers._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands.GetFilingDetails
import hmda.model.filing.{Filing, FilingDetails}
import hmda.persistence.filing.FilingPersistence.selectFiling
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
    pathPrefix("institutions" / Segment / "year" / IntNumber) { (lei, year) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        timedGet { uri =>
          pathEndOrSingleSlash {
            obtainAllFilingDetailsRoute(lei, year, uri)
          } ~ path("quarter" / Quarter) { quarter =>
            quarterlyFilingAllowed(lei, year) {
              obtainFilingDetailsRoute(lei, year, Option(quarter), uri)
            }
          }
        }
      }
    }

  private def obtainAllFilingDetailsRoute(lei: String, year: Int, uri: Uri): Route = {
    def obtainFilingDetails(lei: String, year: Int, quarter: Option[String]): Future[Option[FilingDetails]] = {
      val fil = selectFiling(sharding, lei, year, quarter)
      fil ? GetFilingDetails
    }

    val institutionPersistence = selectInstitution(sharding, lei, year)
    val fInstitution: Future[Option[Institution]] = institutionPersistence ? GetInstitution
    val allFilings = {
      val filingsYear = obtainFilingDetails(lei, year, None)
      val filingsQuarters = Future.sequence(List("Q1", "Q2", "Q3").map(q => obtainFilingDetails(lei, year, Some(q))))

      for {
        year <- filingsYear
        quarters <- filingsQuarters
      } yield (year :: quarters).flatten.map(_.filing)
    }

    val details = for {
      ins <- fInstitution
      filings <- allFilings
    } yield (ins, filings)

    onComplete(details) {
      case Failure(error) =>
        failedResponse(InternalServerError, uri, error)

      case Success((None, _)) =>
        val errorResponse =
          ErrorResponse(404, s"Institution: $lei does not exist", uri.path)
        complete(NotFound -> errorResponse)

      case Success((ins @ Some(_), filings)) =>
        complete(InstitutionDetail(ins, filings))
    }
  }

  private def obtainFilingDetailsRoute(lei: String, year: Int, quarter: Option[String], uri: Uri): Route = {
    val institutionPersistence                      = selectInstitution(sharding, lei, year)
    val fInstitution: Future[Option[Institution]] = institutionPersistence ? (ref => GetInstitution(ref))
    val fil = selectFiling(sharding, lei, year, quarter)
    val fDetails: Future[Option[FilingDetails]] = fil ? (ref => GetFilingDetails(ref))
    val iDetails = for {
      institution <- fInstitution
      filings <- fDetails
    } yield(institution, filings)

    onComplete(iDetails) {
      case Success((i @ Some(_), optFilings)) =>
        complete(InstitutionDetail(i, optFilings.map(_.filing).toList))
      case Success((i @ Some(_), None)) =>
        complete(InstitutionDetail(i, List(Filing())))
      case Success((None,_)) =>
        val errorResponse = ErrorResponse(404, s"Institution: $lei does not exist", uri.path)
        complete(NotFound -> errorResponse)

      case Failure(error) =>
        failedResponse(InternalServerError, uri, error)
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