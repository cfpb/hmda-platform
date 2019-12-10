package hmda.dashboard.api

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpEntity, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.dashboard.Settings
//import hmda.dashbaord.api.DataBrowserDirectives._
//import hmda.dashbaord.models.HealthCheckStatus.Up
//import hmda.dashbaord.models._
//import hmda.dashbaord.repositories._
//import hmda.dashbaord.services._
//import io.lettuce.core.api.async.RedisAsyncCommands
//import io.lettuce.core.{ClientOptions, RedisClient}
//import slick.basic.DatabaseConfig
//import slick.jdbc.JdbcProfile

//import scala.util.{Failure, Success}

trait HmdaDashboardHttpApi extends Settings {

  val log: LoggingAdapter

  val hmdaDashboardRoutes: Route =
    encodeResponse {
      pathPrefix("view") {
        log.info("hmda-dashboard")
        complete(StatusCodes.OK)
      }
    }
}