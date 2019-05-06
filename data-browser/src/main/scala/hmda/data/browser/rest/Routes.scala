package hmda.data.browser.rest

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import DataBrowserDirectives._
import hmda.data.browser.models._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.services.BrowserService
import monix.execution.{Scheduler => MonixScheduler}

object Routes {
  def apply(browserService: BrowserService)(
      implicit scheduler: MonixScheduler): Route = {
    pathPrefix("data-browser" / "view") {
      path("msamd" / MsaMdSegment) { msamd =>
        pathEndOrSingleSlash {
          (extractActions & extractRaces) {
            (actionsTaken: Seq[ActionTaken], races: Seq[Race]) =>
              get {
                val inputParameters = Parameters(msaMd = Some(msamd.msaMd),
                                                 state = None,
                                                 races = races.map(_.entryName),
                                                 actionsTaken =
                                                   actionsTaken.map(_.value))

                val stats =
                  browserService
                    .fetchAggregate(msamd, races, actionsTaken)
                    .map(aggs => AggregationResponse(inputParameters, aggs))
                    .runToFuture
                complete(OK, stats)
              }
          }
        }
      } ~
        path("state" / StateSegment) { state: State =>
          pathEndOrSingleSlash {
            (extractActions & extractRaces) { (actionsTaken, races) =>
              get {
                val inputParameters = Parameters(msaMd = None,
                                                 state = Some(state.entryName),
                                                 races = races.map(_.entryName),
                                                 actionsTaken =
                                                   actionsTaken.map(_.value))

                val stats =
                  browserService
                    .fetchAggregate(state, races, actionsTaken)
                    .map(aggs => AggregationResponse(inputParameters, aggs))
                    .runToFuture
                complete(OK, stats)
              }
            }
          }
        } ~
        path("nationwide") {
          pathEndOrSingleSlash {
            (extractActions & extractRaces) { (actionsTaken, races) =>
              get {
                val inputParameters = Parameters(msaMd = None,
                                                 state = None,
                                                 races = races.map(_.entryName),
                                                 actionsTaken =
                                                   actionsTaken.map(_.value))

                val stats =
                  browserService
                    .fetchAggregate(races, actionsTaken)
                    .map(aggs => AggregationResponse(inputParameters, aggs))
                    .runToFuture
                complete(OK, stats)
              }
            }
          }
        }
    }
  }
}