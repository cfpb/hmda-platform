package hmda.reporting.api.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.institution.{HmdaFiler, HmdaFilerResponse, MsaMd, MsaMdResponse}
import hmda.query.repository.ModifiedLarRepository
import hmda.reporting.repository.InstitutionComponent
import hmda.util.http.FilingResponseUtils.entityNotPresentResponse
import io.circe.generic.auto._
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
trait ReportingHttpApi extends InstitutionComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  val config = ConfigFactory.load()
  val bankFilter = config.getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")
  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  val repo = new ModifiedLarRepository(databaseConfig)
  val institutionRepository: InstitutionRepository
  val log = LoggerFactory.getLogger("hmda")
  val filerListRoute: Route = {
    path("filers" / IntNumber) { filingYear =>
      log.info("Fileres ")
      get {

        val futFilerSet =
          institutionRepository
            .getFilteredFilers(bankFilterList)
            .map(
              sheets =>
                sheets
                  .map(instituionEntity =>
                    HmdaFiler(instituionEntity.lei.toUpperCase,
                              instituionEntity.respondentName,
                              instituionEntity.activityYear.toString))
                  .toSet)
        onComplete(futFilerSet) {
          case Success(filerSet) =>
            complete(HmdaFilerResponse(filerSet))
          case Failure(error) =>
            complete(StatusCodes.BadRequest -> error.getLocalizedMessage)
        }

      }
    } ~ path("filers" / IntNumber / Segment / "msaMds") { (year, lei) =>
      extractUri { uri =>
        log.info("msaMds endpoint with lei: " + lei)
        val resultset = for {
          msaMdsResult <- repo.msaMds(lei, year)
          institutionResult <- institutionRepository.findByLei(lei)
        } yield {
          val msaMds =
            msaMdsResult.map(myEntity => MsaMd(myEntity._1, myEntity._2)).toSet
          MsaMdResponse(
            new HmdaFiler(institutionResult.head.lei.toUpperCase,
                          institutionResult.head.respondentName,
                          institutionResult.head.activityYear.toString),
            msaMds)
        }

        onComplete(resultset) {
          case Success(leiMsaMds) =>
            complete(leiMsaMds)
          case Failure(error) =>
            log.error("Errored in the /msaMds endpoint with: " + error)
            entityNotPresentResponse("institution", lei, uri)
        }
      }
    }

  }

  def hmdaFilerRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          filerListRoute
        }
      }
    }
  }
}
