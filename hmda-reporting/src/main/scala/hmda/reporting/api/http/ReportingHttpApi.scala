package hmda.reporting.api.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{ cors, corsRejectionHandler }
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.institution.{ HmdaFiler, HmdaFilerResponse, MsaMd, MsaMdResponse }
import hmda.query.repository.ModifiedLarRepository
import hmda.reporting.repository.InstitutionComponent
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
  private val repo                      = new ModifiedLarRepository(databaseConfig)
  private val institutionRepository2018 = new InstitutionRepository(databaseConfig, "institutions2018")
  private val institutionRepository2019 = new InstitutionRepository(databaseConfig, "institutions2019")
  private val institutionRepository2020 = new InstitutionRepository(databaseConfig, "institutions2020")
  private val institutionRepository2021 = new InstitutionRepository(databaseConfig, "institutions2021")
  private val institutionRepository2022 = new InstitutionRepository(databaseConfig, "institutions2022")
  private val institutionRepository2023 = new InstitutionRepository(databaseConfig, "institutions2023")
  private val institutionRepository2024 = new InstitutionRepository(databaseConfig, "institutions2024")


  private val filerListRoute: Route = {
    path("filers" / IntNumber) { filingYear =>
      get {

        val futFilerSet: Future[Set[HmdaFiler]] = filingYear match {
          case 2018 =>
            institutionRepository2018
              .getFilteredFilers(bankFilterList)
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
          case 2019 =>
            institutionRepository2019
              .getFilteredFilers(bankFilterList)
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
          case 2020 =>
            institutionRepository2020
              .getFilteredFilers(bankFilterList)
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
          case 2021 =>
            institutionRepository2021
              .getFilteredFilers(bankFilterList)
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

          case 2022 =>
            institutionRepository2022
              .getFilteredFilers(bankFilterList)
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

          case 2023 =>
            institutionRepository2023
              .getFilteredFilers(bankFilterList)
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
          case 2024 =>
            institutionRepository2024
              .getFilteredFilers(bankFilterList)
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
          case _ => Future(Set(HmdaFiler("", "", "")))
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
        val institutionRepository =
          year match {
            case 2018 => institutionRepository2018
            case 2019 => institutionRepository2019
            case 2020 => institutionRepository2020
            case 2021 => institutionRepository2021
            case 2022 => institutionRepository2022
            case 2023 => institutionRepository2023
            case 2024 => institutionRepository2024



          }
        val resultset = for {
          msaMdsResult      <- repo.msaMds(lei, year)
          institutionResult <- institutionRepository.findByLei(lei)
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