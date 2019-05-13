package hmda.data.browser.rest

import akka.http.scaladsl.model.ContentTypes.`text/csv(UTF-8)`
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.models._
import hmda.data.browser.rest.DataBrowserDirectives._
import hmda.data.browser.services.BrowserService
import monix.execution.{Scheduler => MonixScheduler}

object Routes {
  def apply(browserService: BrowserService)(implicit scheduler: MonixScheduler,
                                            mat: ActorMaterializer): Route = {
    def stateAndMsaRoute(state: State,
                         msaMd: MsaMd,
                         races: Seq[Race],
                         actionsTaken: Seq[ActionTaken]): Route =
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(
              browserService.fetchData(msaMd, state, races, actionsTaken))
          )
        )
      } ~ get {
        val inputParameters = Parameters(msaMd = Some(msaMd.msaMd),
                                         state = Some(state.entryName),
                                         races = races.map(_.entryName),
                                         actionsTaken =
                                           actionsTaken.map(_.value))

        val stats =
          browserService
            .fetchAggregate(msaMd, state, races, actionsTaken)
            .map(aggs => AggregationResponse(inputParameters, aggs))
            .runToFuture
        complete(OK, stats)
      }

    def stateRoute(state: State,
                   races: Seq[Race],
                   actionsTaken: Seq[ActionTaken]): Route =
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(browserService.fetchData(state, races, actionsTaken))
          )
        )
      } ~ get {
        val inputParameters =
          Parameters(msaMd = None,
                     state = Some(state.entryName),
                     races = races.map(_.entryName),
                     actionsTaken = actionsTaken.map(_.value))

        val stats =
          browserService
            .fetchAggregate(state, races, actionsTaken)
            .map(aggs => AggregationResponse(inputParameters, aggs))
            .runToFuture
        complete(OK, stats)
      }

    def nationwideRoute(races: Seq[Race],
                        actionsTaken: Seq[ActionTaken]): Route =
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(
              browserService
                .fetchData(races, actionsTaken))
          )
        )
      } ~ get {
        val inputParameters =
          Parameters(msaMd = None,
                     state = None,
                     races = races.map(_.entryName),
                     actionsTaken = actionsTaken.map(_.value))

        val stats =
          browserService
            .fetchAggregate(races, actionsTaken)
            .map(aggs => AggregationResponse(inputParameters, aggs))
            .runToFuture
        complete(OK, stats)
      }

    def msaRoute(msaMd: MsaMd,
                 races: Seq[Race],
                 actionsTaken: Seq[ActionTaken]): Route =
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(browserService.fetchData(msaMd, races, actionsTaken))
          )
        )
      } ~ get {
        val inputParameters = Parameters(msaMd = Some(msaMd.msaMd),
                                         state = None,
                                         races = races.map(_.entryName),
                                         actionsTaken =
                                           actionsTaken.map(_.value))

        val stats =
          browserService
            .fetchAggregate(msaMd, races, actionsTaken)
            .map(aggs => AggregationResponse(inputParameters, aggs))
            .runToFuture
        complete(OK, stats)
      }

    // TODO: Add invalidate endpoints
    pathPrefix("view") {
      // ?actions_taken=1&races=Asian
      (extractActions & extractRaces) { (actionsTaken, races) =>
        // /data-browser-api/view/nationwide(/csv)
        pathPrefix("nationwide")(nationwideRoute(races, actionsTaken)) ~
          // /data-browser-api/view/msamd/<msamd>(/csv)
          pathPrefix("msamd" / MsaMdSegment) { msaMd =>
            msaRoute(msaMd, races, actionsTaken)
          } ~
          pathPrefix("state" / StateSegment) { state =>
            // /data-browser-api/view/state/<state>/msamd/<msamd>(/csv)
            pathPrefix("msamd" / MsaMdSegment) { msaMd =>
              stateAndMsaRoute(state, msaMd, races, actionsTaken)
            } ~
              // /data-browser-api/view/state/<state>(/csv)
              stateRoute(state, races, actionsTaken)
          }
      }
    }
  }
}
