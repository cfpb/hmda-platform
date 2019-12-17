package hmda.dashboard.api

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.dashboard.Settings
import hmda.dashboard.models.HealthCheckStatus.Up
import hmda.dashboard.models._
import hmda.dashboard.repositories._
import hmda.dashboard.services._
import monix.execution.Scheduler.Implicits.global
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.util.{Failure, Success}

trait HmdaDashboardHttpApi extends Settings {

  val log: LoggingAdapter
  implicit val materializer: ActorMaterializer

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("dbconfig")
  val repository =
    new PostgresRepository(database.tableName, databaseConfig)

  val healthCheck: HealthCheckService =
    new HealthCheckService(repository)

  val query: QueryService =
    new DashboardQueryService(repository)

  val hmdaDashboardRoutes: Route =
    encodeResponse {
      pathPrefix("dashboard") {
        pathPrefix("health") {
          onComplete(healthCheck.healthCheckStatus.runToFuture) {
            case Success(HealthCheckResponse(Up)) =>
              log.info("hmda-dashboard health check OK")
              complete(StatusCodes.OK)

            case Success(hs) =>
              log.warning(s"Service degraded db=${hs.db}")
              complete(StatusCodes.ServiceUnavailable)

            case Failure(ex) =>
              log.error(ex, "Failed to perform a health check")
              complete(StatusCodes.InternalServerError)
          }
        }~
        path("total_filers" / IntNumber) { (year) =>
          log.info(s"total filers for year=${year}")
            complete(
              query
                .fetchTotalFilers(year)
                .map(aggs => TotalFilersAggregationResponse(aggs))
                .runToFuture
            )
        }~
        path("total_lars" / IntNumber) { (year) =>
          log.info(s"total lars for year=${year}")
          complete(
            query
              .fetchTotalLars(year)
              .map(aggs => TotalLarsAggregationResponse(aggs))
              .runToFuture
          )
        }~
        path("single_attempts" / IntNumber) { (year) =>
          log.info(s"single attempts for year=${year}")
          complete(
            query
              .fetchSingleAttempts(year)
              .map(aggs => SingleAttemptsAggregationResponse(aggs))
              .runToFuture
          )
        }~
        path("multiple_attempts" / IntNumber) { (year) =>
          log.info(s"multiple attempts for year=${year}")
          complete(
            query
              .fetchMultipleAttempts(year)
              .map(aggs => MultipleAttemptsAggregationResponse(aggs))
              .runToFuture
          )
        }~
          path("top_filers" / IntNumber / "year" / IntNumber) { (count, year) =>
            log.info(s"top filers (${count}) for year=${year}")
            complete(
              query
                .fetchTopFilers(count,year)
                .map(aggs => TopFilersAggregationResponse(aggs))
                .runToFuture
            )
        }~
        path("signs_per_day" / IntNumber / "year" / IntNumber) { (days, year) =>
          log.info(s"filers for last ${days} days for year=${year}")
          complete(
            query
              .fetchSignsForLastDays(days,year)
              .map(aggs => SignsForLastDaysAggregationResponse(aggs))
              .runToFuture
          )
        }~
        path("filer_attempts" / IntNumber / "year" / IntNumber) { (count, year) =>
          log.info(s"top ${count} filers  attempts for year=${year}")
          complete(
            query
              .fetchFilerAttempts(count,year)
              .map(aggs => FilerAttemptsAggregationResponse(aggs))
              .runToFuture
          )
        }~
          path("total_ts" / IntNumber) { (year) =>
            log.info(s"TS Records for year=${year}")
            complete(
              query
                .fetchTSRecordCount(year)
                .map(aggs => TSRecordCountAggregationResponse(aggs))
                .runToFuture
            )
        }~
        path("filers_by_agency" / IntNumber) { (year) =>
          log.info(s"Filers by agency code for year=${year}")
          complete(
            query
              .fetchFilersByAgency(year)
              .map(aggs => FilersByAgencyAgggregationResponse(aggs))
              .runToFuture
          )
        }~
        path("lar_by_agency" / IntNumber) { (year) =>
          log.info(s"Lar by agency code for year=${year}")
          complete(
            query
              .fetchLARByAgency(year)
              .map(aggs => LarByAgencyAggregationResponse(aggs))
              .runToFuture
          )
        }~
        path("top_counties_lar" / IntNumber / "year" / IntNumber) { (count, year) =>
          log.info(s"Top ${count} counties for year=${year}")
          complete(
            query
              .fetchTopCountiesLar(count, year)
              .map(aggs => TopCountiesLarAggregationResponse(aggs))
              .runToFuture
          )
        }~
          path("lar_by_property" / IntNumber) { (year) =>
            log.info(s"Lar count by property for year=${year}")
            complete(
              query
                .fetchLarCountByPropertyType(year)
                .map(aggs => LarCountByPropertyTypeAggregationResponse(aggs))
                .runToFuture
            )
          }
      }
    }
}