package hmda.dashboard.api

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.dashboard.Settings
//import hmda.dashboard.api.DataBrowserDirectives._
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
        }
      }
    }
}