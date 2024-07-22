package hmda.api.http.filing

import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.PathMatchers._
import hmda.api.http.directives.QuarterlyFilingAuthorization.quarterlyFilingAllowed
import hmda.api.http.model.ErrorResponse
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands.GetFilingDetails
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.filing.FilingDetails
import hmda.model.institution.{ Institution, InstitutionDetail }
import hmda.persistence.filing.FilingPersistence.selectFiling
import hmda.persistence.institution.InstitutionPersistence.selectInstitution
import hmda.util.http.FilingResponseUtils._
import org.slf4j.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
// $COVERAGE-OFF$
object InstitutionHttpApi {
  def create(log: Logger, sharding: ClusterSharding)(implicit timeout: Timeout, ec: ExecutionContext): OAuth2Authorization => Route =
    new InstitutionHttpApi(log, sharding)(timeout, ec).institutionRoutes _
}

private class InstitutionHttpApi(log: Logger, sharding: ClusterSharding)(implicit val timeout: Timeout, ec: ExecutionContext) {
  private val quarterlyFiler = quarterlyFilingAllowed(log, sharding) _

  // GET /institutions/<lei>/year/<y>
  // GET /institutions/<lei>/year/<y>/quarter/<q>
  def institutionReadPath(oAuth2Authorization: OAuth2Authorization): Route =
    pathPrefix("institutions" / Segment / "year" / IntNumber) { (lei, year) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        (extractUri & get) { uri =>
          pathEndOrSingleSlash {
            obtainAllFilingDetailsRoute(lei, year, uri)
          } ~ path("quarter" / Quarter) { quarter =>
            quarterlyFiler(lei, year) {
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

    val institutionPersistence                    = selectInstitution(sharding, lei, year)
    val fInstitution: Future[Option[Institution]] = institutionPersistence ? GetInstitution
    val allFilings = {
      val filingsYear     = obtainFilingDetails(lei, year, None)
      val filingsQuarters = Future.sequence(List("Q1", "Q2", "Q3").map(q => obtainFilingDetails(lei, year, Some(q))))

      for {
        year     <- filingsYear
        quarters <- filingsQuarters
      } yield (year :: quarters).flatten.map(_.filing)
    }

    val details = for {
      ins     <- fInstitution
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
    val institutionPersistence                    = selectInstitution(sharding, lei, year)
    val fInstitution: Future[Option[Institution]] = institutionPersistence ? (ref => GetInstitution(ref))
    val fil                                       = selectFiling(sharding, lei, year, quarter)
    val fDetails: Future[Option[FilingDetails]]   = fil ? (ref => GetFilingDetails(ref))
    val iDetails = for {
      institution <- fInstitution
      filings     <- fDetails
    } yield (institution, filings)

    onComplete(iDetails) {
      case Success((i @ Some(_), optFilings)) =>
        complete(InstitutionDetail(i, optFilings.map(_.filing).toList))
      case Success((None, _)) =>
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
// $COVERAGE-ON$