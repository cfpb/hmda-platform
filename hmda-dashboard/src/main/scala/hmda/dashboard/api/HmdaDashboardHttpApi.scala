package hmda.dashboard.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.auth.OAuth2Authorization
import hmda.dashboard.Settings
import hmda.dashboard.models.HealthCheckStatus.Up
import hmda.dashboard.models._
import hmda.dashboard.repositories._
import hmda.dashboard.services._
import monix.execution.Scheduler.Implicits.global
import org.slf4j.Logger
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object HmdaDashboardHttpApi {
  def create(log: Logger, config: Config)(implicit timeout: Timeout, ec: ExecutionContext): OAuth2Authorization => Route =
    new HmdaDashboardHttpApi(log, config)(timeout, ec).hmdaDashboardRoutes _
}

private class HmdaDashboardHttpApi(log: Logger, config: Config)(implicit val timeout: Timeout, ec: ExecutionContext) extends Settings {

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("dashboard_db")
  val hmdaAdminRole = config.getString("keycloak.hmda.admin.role")
  val bankFilter     = ConfigFactory.load("application.conf").getConfig("filter")
    val bankFilterList =
      bankFilter.getString("bank-filter-list").toUpperCase.split(",")
    val repository =
      new PostgresRepository(databaseConfig, bankFilterList)

    val healthCheck: HealthCheckService =
      new HealthCheckService(repository)

    val query: QueryService =
      new DashboardQueryService(repository)

    def hmdaDashboardReadPath(oAuth2Authorization: OAuth2Authorization): Route = {
      oAuth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
        withoutRequestTimeout {
        pathPrefix("dashboard") {
          pathPrefix("health") {
            onComplete(healthCheck.healthCheckStatus.runToFuture) {
              case Success(HealthCheckResponse(Up)) =>
                log.info("hmda-dashboard health check OK")
                complete(StatusCodes.OK)

              case Success(hs) =>
                log.info(s"Service degraded db=${hs.db}")
                complete(StatusCodes.ServiceUnavailable)

              case Failure(ex) =>
                log.error("Failed to perform a health check", ex)
                complete(StatusCodes.InternalServerError)
            }
          } ~
            path("total_filers" / IntNumber) { (year) =>
              log.info(s"total filers for year=${year}")
              complete(
                query
                  .fetchTotalFilers(year)
                  .map(aggs => TotalFilersAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("total_lars" / IntNumber) { (year) =>
              log.info(s"total lars for year=${year}")
              complete(
                query
                  .fetchTotalLars(year)
                  .map(aggs => TotalLarsAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("single_attempts" / IntNumber) { (year) =>
              log.info(s"single attempts for year=${year}")
              complete(
                query
                  .fetchSingleAttempts(year)
                  .map(aggs => SingleAttemptsAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("multiple_attempts" / IntNumber) { (year) =>
              log.info(s"multiple attempts for year=${year}")
              complete(
                query
                  .fetchMultipleAttempts(year)
                  .map(aggs => MultipleAttemptsAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("top_filers" / IntNumber / "year" / IntNumber) { (count, year) =>
              log.info(s"top filers (${count}) for year=${year}")
              complete(
                query
                  .fetchTopFilers(count, year)
                  .map(aggs => TopFilersAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("signs_per_day" / IntNumber / "year" / IntNumber) { (days, year) =>
              log.info(s"filers for last ${days} days for year=${year}")
              complete(
                query
                  .fetchSignsForLastDays(days, year)
                  .map(aggs => SignsForLastDaysAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filer_attempts" / IntNumber / "year" / IntNumber) { (count, year) =>
              log.info(s"top ${count} filers  attempts for year=${year}")
              complete(
                query
                  .fetchFilerAttempts(count, year)
                  .map(aggs => FilerAttemptsAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("total_ts" / IntNumber) { (year) =>
              log.info(s"TS Records for year=${year}")
              complete(
                query
                  .fetchTSRecordCount(year)
                  .map(aggs => TSRecordCountAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_by_agency" / IntNumber) { (year) =>
              log.info(s"Filers by agency code for year=${year}")
              complete(
                query
                  .fetchFilersByAgency(year)
                  .map(aggs => FilersByAgencyAgggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("lar_by_agency" / IntNumber) { (year) =>
              log.info(s"Lar by agency code for year=${year}")
              complete(
                query
                  .fetchLARByAgency(year)
                  .map(aggs => LarByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("top_counties_lar" / IntNumber / "year" / IntNumber) { (count, year) =>
              log.info(s"Top ${count} counties for year=${year}")
              complete(
                query
                  .fetchTopCountiesLar(count, year)
                  .map(aggs => TopCountiesLarAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("lar_by_property" / IntNumber) { (year) =>
              log.info(s"Lar count by property for year=${year}")
              complete(
                query
                  .fetchLarCountByPropertyType(year)
                  .map(aggs => LarCountByPropertyTypeAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_using_exemption_by_agency" / IntNumber) { (year) =>
              log.info(s"Filers using exemption by agency for year=${year}")
              complete(
                query
                  .fetchFilersUsingExemptionsByAgency(year)
                  .map(aggs => FilersUsingExemptionByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("denial_reason_counts_by_agency" / IntNumber) { (year) =>
              log.info(s"Denial Reason Counts By Agency for year=${year}")
              complete(
                query
                  .fetchDenialReasonCountsByAgency(year)
                  .map(aggs => DenialReasonCountsByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("lar_count_using_exemption_by_agency" / IntNumber) { (year) =>
              log.info(s"Lar Count Using Exemption By Agency for year=${year}")
              complete(
                query
                  .fetchLarCountUsingExemptionByAgency(year)
                  .map(aggs => LarCountUsingExemptionByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("open_end_credit_filers_by_agency" / IntNumber) { (year) => // TODO: fix
              log.info(s"Open end credit By Agency for year=${year}")
              complete(
                query
                  .fetchOpenEndCreditFilersByAgency(year)
                  .map(aggs => OpenEndCreditByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("open_end_credit_lar_count_by_agency" / IntNumber) { (year) => // TODO: fix
              log.info(s"Open end credit Lar count By Agency for year=${year}")
              complete(
                query
                  .fetchOpenEndCreditLarCountByAgency(year)
                  .map(aggs => OpenEndCreditLarCountByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_with_only_open_end_credit_transactions" / IntNumber) { (year) =>
              log.info(s"Filers With Only Open End Credit Transactions for year=${year}")
              complete(
                query
                  .fetchFilersWithOnlyOpenEndCreditTransactions(year)
                  .map(aggs => FilersWithOnlyOpenEndCreditTransactionsAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_with_only_closed_end_credit_transactions" / IntNumber) { (year) =>
              log.info(s"Filers With Only Closed End Credit Transactions for year=${year}")
              complete(
                query
                  .fetchFilersWithOnlyClosedEndCreditTransactions(year)
                  .map(aggs => FilersWithOnlyClosedEndCreditTransactionsAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filer_list_only_open_end_transactions" / IntNumber) { (year) =>
              log.info(s"Filers With Only Closed End Credit Transactions for year=${year}")
              complete(
                query
                  .fetchFilersListWithOnlyOpenEndCreditTransactions(year)
                  .map(aggs => FilersListWithOnlyOpenEndCreditAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_claiming_exemption" / IntNumber) { (year) =>
              log.info(s"Filers claiming exemptions for year=${year}")
              complete(
                query
                  .fetchFilersClaimingExemption(year)
                  .map(aggs => FilersClaimingExemptionAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("list_quarterly_filers" / IntNumber) { (year) =>
              log.info(s"List quarterly filers for year=${year}")
              complete(
                query
                  .fetchListQuarterlyFilers(year)
                  .map(aggs => ListQuarterlyFilersAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_by_week_by_agency" / IntNumber / "week" / IntNumber) { (year, week) =>
              log.info(s"Filers for year=${year} for week=${week}")
              complete(
                query
                  .fetchFilersByWeekByAgency(year, week)
                  .map(aggs => FilersByWeekByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("lar_by_week_by_agency" / IntNumber / "week" / IntNumber) { (year, week) =>
              log.info(s"LAR for year=${year} for week=${week}")
              complete(
                query
                  .fetchLarByWeekByAgency(year, week)
                  .map(aggs => LarByWeekByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("list_filers_with_only_closed_end_credit_transactions" / IntNumber) { (year) =>
              log.info(s"List filers with only closed end credit transactions for year=${year}")
              complete(
                query
                  .fetchListFilersWithOnlyClosedEndCreditTransactions(year)
                  .map(aggs => ListFilersWithOnlyClosedEndCreditTransactionsAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_count_closed_end_originations_by_agency" / IntNumber / "thresh" / IntNumber) { (year, x) =>
              log.info(s"Fetch filers count closed end originations by agency for year=${year} with threshehold<${x}")
              complete(
                query
                  .fetchFilersCountClosedEndOriginationsByAgency(year, x)
                  .map(aggs => FilersCountClosedEndOriginationsByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_count_closed_end_originations_by_agency_grater_or_equal" / IntNumber / "thresh" / IntNumber) { (year, x) =>
              log.info(s"Fetch filers count closed end originations by agency for year=${year} with threshehold=>${x}")
              complete(
                query
                  .fetchFilersCountClosedEndOriginationsByAgencyGraterOrEqual(year, x)
                  .map(aggs => FilersCountClosedEndOriginationsByAgencyGraterThanEqualAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_count_open_end_originations_by_agency" / IntNumber / "thresh" / IntNumber) { (year, x) =>
              log.info(s"Fetch filers count open end originations by agency for year=${year} with threshehold<${x}")
              complete(
                query
                  .fetchFilersCountOpenEndOriginationsByAgency(year, x)
                  .map(aggs => FilersCountOpenEndOriginationsByAgencyAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("filers_count_open_end_originations_by_agency_grater_or_equal" / IntNumber / "thresh" / IntNumber) { (year, x) =>
              log.info(s"Fetch filers count open end originations by agency for year=${year} with threshehold>=${x}")
              complete(
                query
                  .fetchFilersCountOpenEndOriginationsByAgencyGraterOrEqual(year, x)
                  .map(aggs => FilersCountOpenEndOriginationsByAgencyGraterOrEqualAggregationResponse(aggs))
                  .runToFuture
              )
            } ~
            path("top_institutions_count_open_end_credit" / IntNumber / "count" / IntNumber) { (year, x) =>
              log.info(s"Top institutions count with open end credit for year=${year} limit=${x}")
              complete(
                query
                  .fetchTopInstitutionsCountOpenEndCredit(year, x)
                  .map(aggs => TopInstitutionsCountOpenEndCreditAggregationResponse(aggs))
                  .runToFuture
              )
            }
          }
        }
      }
    }

    def hmdaDashboardRoutes(oAuth2Authorization: OAuth2Authorization): Route = {
      handleRejections(corsRejectionHandler) {
        cors() {
          encodeResponse {
            hmdaDashboardReadPath(oAuth2Authorization)
          }
        }
      }
    }
}
