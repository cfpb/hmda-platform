package hmda.data.browser.rest

import akka.http.scaladsl.model.ContentTypes.`text/csv(UTF-8)`
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.models._
import hmda.data.browser.models.BrowserField
import hmda.data.browser.rest.DataBrowserDirectives._
import hmda.data.browser.services.BrowserService
import monix.execution.{Scheduler => MonixScheduler}
import akka.http.scaladsl.model.StatusCodes.BadRequest

object Routes {
  def apply(browserService: BrowserService)(
      implicit scheduler: MonixScheduler): Route = {
    def stateAndMsaRoute(state: State,
                         msaMd: MsaMd,
                         actionsTaken: BrowserField,
                         race: BrowserField): Route = {
      val filteredfields = List(actionsTaken, race).filter(_.name != "empty")
      if (filteredfields.length > 2) {
        complete((BadRequest, "More than 2 fields provided"))
      } else {

        if (filteredfields.length == 2) {
          val sortedFields = filteredfields.sortBy(_.name)
          val field1 = sortedFields.head
          val field2 = sortedFields.last

          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(
                  browserService.fetchData(msaMd, state, field1, field2))
              )
            )
          } ~ get {
            val inputParameters = Parameters(msaMd = Some(msaMd.msaMd),
                                             state = Some(state.entryName),
                                             field1 = field1,
                                             field2 = field2)

            val stats =
              browserService
                .fetchAggregate(msaMd, state, field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        } else {
          val field1 = filteredfields.head
          val field2 = BrowserField("raceApplicant1",
                                    List("*"),
                                    "raceApplicant1",
                                    "raceApplicant1")
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(
                  browserService.fetchData(msaMd, state, field1, field2))
              )
            )
          } ~ get {
            val inputParameters = Parameters(msaMd = Some(msaMd.msaMd),
                                             state = Some(state.entryName),
                                             field1 = field1,
                                             field2 = field2)

            val stats =
              browserService
                .fetchAggregate(msaMd, state, field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        }
      }
    }

    def stateRoute(state: State,
                   actionsTaken: BrowserField,
                   race: BrowserField): Route = {
      val filteredfields = List(actionsTaken, race).filter(_.name != "empty")
      if (filteredfields.length > 2) {
        complete((BadRequest, "More than 2 fields provided"))
      } else {

        if (filteredfields.length == 2) {
          val sortedFields = filteredfields.sortBy(_.name)
          val field1 = sortedFields.head
          val field2 = sortedFields.last
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(browserService.fetchData(state, field1, field2))
              )
            )
          } ~ get {
            val inputParameters =
              Parameters(msaMd = None,
                         state = Some(state.entryName),
                         field1 = field1,
                         field2 = field2)

            val stats =
              browserService
                .fetchAggregate(state, field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        } else {
          val field1 = filteredfields.head
          val field2 = BrowserField("raceApplicant1",
                                    List("*"),
                                    "raceApplicant1",
                                    "raceApplicant1")
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(browserService.fetchData(state, field1, field2))
              )
            )
          } ~ get {
            val inputParameters =
              Parameters(msaMd = None,
                         state = Some(state.entryName),
                         field1 = field1,
                         field2 = field2)

            val stats =
              browserService
                .fetchAggregate(state, field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        }
      }
    }

    def nationwideRoute(actionsTaken: BrowserField,
                        race: BrowserField): Route = {
      val filteredfields = List(actionsTaken, race).filter(_.name != "empty")
      if (filteredfields.length > 2) {
        complete((BadRequest, "More than 2 fields provided"))
      } else {

        if (filteredfields.length == 2) {
          val sortedFields = filteredfields.sortBy(_.name)
          val field1 = sortedFields.head
          val field2 = sortedFields.last
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(
                  browserService
                    .fetchData(field1, field2))
              )
            )
          } ~ get {
            val inputParameters =
              Parameters(msaMd = None,
                         state = None,
                         field1 = field1,
                         field2 = field2)

            val stats =
              browserService
                .fetchAggregate(field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        } else {
          val field1 = filteredfields.head
          val field2 = BrowserField("raceApplicant1",
                                    List("*"),
                                    "raceApplicant1",
                                    "raceApplicant1")
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(
                  browserService
                    .fetchData(field1, field2))
              )
            )
          } ~ get {
            val inputParameters =
              Parameters(msaMd = None,
                         state = None,
                         field1 = field1,
                         field2 = field2)

            val stats =
              browserService
                .fetchAggregate(field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        }
      }
    }

    def msaRoute(msaMd: MsaMd,
                 actionsTaken: BrowserField,
                 race: BrowserField): Route = {
      val filteredfields = List(actionsTaken, race).filter(_.name != "empty")
      if (filteredfields.length > 2) {
        complete((BadRequest, "More than 2 fields provided"))
      } else {

        if (filteredfields.length == 2) {
          val sortedFields = filteredfields.sortBy(_.name)
          val field1 = sortedFields.head
          val field2 = sortedFields.last
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(browserService.fetchData(msaMd, field1, field2))
              )
            )
          } ~ get {
            val inputParameters = Parameters(msaMd = Some(msaMd.msaMd),
                                             state = None,
                                             field1 = field1,
                                             field2 = field2)

            val stats =
              browserService
                .fetchAggregate(msaMd, field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        } else {
          val field1 = filteredfields.head
          val field2 = BrowserField("raceApplicant1",
                                    List("*"),
                                    "raceApplicant1",
                                    "raceApplicant1")
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/csv(UTF-8)`,
                csvSource(browserService.fetchData(msaMd, field1, field2))
              )
            )
          } ~ get {
            val inputParameters = Parameters(msaMd = Some(msaMd.msaMd),
                                             state = None,
                                             field1 = field1,
                                             field2 = field2)

            val stats =
              browserService
                .fetchAggregate(msaMd, field1, field2)
                .map(aggs => AggregationResponse(inputParameters, aggs))
                .runToFuture
            complete((OK, stats))
          }
        }
      }
    }

    // TODO: Add invalidate endpoints
    pathPrefix("view") {
      // ?actions_taken=1&races=Asian
      (extractActions & extractRaces) { (actionsTaken, races) =>
        // /data-browser-api/view/nationwide(/csv)
        pathPrefix("nationwide")(nationwideRoute(actionsTaken, races)) ~
          // /data-browser-api/view/msamd/<msamd>(/csv)
          pathPrefix("msamd" / MsaMdSegment) { msaMd =>
            msaRoute(msaMd, actionsTaken, races)
          } ~
          pathPrefix("state" / StateSegment) { state =>
            // /data-browser-api/view/state/<state>/msamd/<msamd>(/csv)
            pathPrefix("msamd" / MsaMdSegment) { msaMd =>
              stateAndMsaRoute(state, msaMd, actionsTaken, races)
            } ~
              // /data-browser-api/view/state/<state>(/csv)
              stateRoute(state, actionsTaken, races)
          }
      }
    }
  }
}
