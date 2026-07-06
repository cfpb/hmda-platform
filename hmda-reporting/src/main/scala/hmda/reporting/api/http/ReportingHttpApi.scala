package hmda.reporting.api.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{ cors, corsRejectionHandler }
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.institution.{ HmdaFiler, HmdaFilerResponse, MsaMd, MsaMdResponse }
import hmda.query.repository.ModifiedLarRepository
import hmda.query.repository.InstitutionComponent
import hmda.util.http.FilingResponseUtils.entityNotPresentResponse
import io.circe.generic.auto._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object ReportingHttpApi {
  def create(config: Config)(implicit ec: ExecutionContext): Route =
    new ReportingHttpApi(config)(ec).hmdaFilerRoutes
}
private class ReportingHttpApi(config: Config)(implicit ec: ExecutionContext) extends InstitutionComponent {

  private val bankFilter                = config.getConfig("filter")
  private val bankFilterList            = bankFilter.getString("bank-filter-list").toUpperCase.split(",")
  private val databaseConfig            = DatabaseConfig.forConfig[JdbcProfile]("db")
  private val modifiedLarRepository     = new ModifiedLarRepository(databaseConfig)
  private val institutionRepository     = new InstitutionRepository(databaseConfig)


  private val filerListRoute: Route = {
    path("filers" / IntNumber) { filingYear =>
      get {

        val futFilerSet: Future[Set[HmdaFiler]] = {
          institutionRepository
            .getFilteredFilers(bankFilterList, filingYear, false)
            .map(sheets =>
              sheets
                .map(instituionEntity =>
                  HmdaFiler(
                    instituionEntity.lei.toUpperCase,
                    instituionEntity.respondentName,
                    instituionEntity.activityYear.toString
                  )
                )
                .toSet
            )
        }

        onComplete(futFilerSet) {
          case Success(filerSet) =>
            complete(HmdaFilerResponse(filerSet))
          case Failure(error) =>
            complete(StatusCodes.BadRequest -> error.getLocalizedMessage)
        }

      }
    } ~ path("filers" / IntNumber / Segment / "msaMds") { (year, lei) =>
      extractUri { uri =>
        val resultset = for {
          msaMdsResult      <- modifiedLarRepository.msaMdsSnapshot(lei, year)
          institutionResult <- institutionRepository.findByLei(lei, year, false)
        } yield {
          val msaMds =
            msaMdsResult.map(myEntity => MsaMd(myEntity._1, myEntity._2)).toSet
          MsaMdResponse(
            new HmdaFiler(
              institutionResult.head.lei.toUpperCase,
              institutionResult.head.respondentName,
              institutionResult.head.activityYear.toString
            ),
            msaMds
          )
        }

        onComplete(resultset) {
          case Success(leiMsaMds) =>
            complete(leiMsaMds)
          case Failure(error) =>
            entityNotPresentResponse("institution", lei, uri)
        }
      }
    }

  }

  def hmdaFilerRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          filerListRoute
        }
      }
    }
}