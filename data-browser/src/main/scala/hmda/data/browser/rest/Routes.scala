package hmda.data.browser.rest

import akka.http.scaladsl.model.ContentTypes._
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
                         field1: BrowserField,
                         field2: BrowserField): Route = {
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(browserService.fetchData(msaMd, state, field1, field2))
          )
        )
      } ~ (path("pipe") & get) {
        complete(
          HttpEntity(
            `text/plain(UTF-8)`,
            pipeSource(browserService.fetchData(msaMd, state, field1, field2))
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

    def stateRoute(state: State,
                   field1: BrowserField,
                   field2: BrowserField): Route = {
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(browserService.fetchData(state, field1, field2))
          )
        )
      } ~ (path("pipe") & get) {
        complete(
          HttpEntity(
            `text/plain(UTF-8)`,
            pipeSource(browserService.fetchData(state, field1, field2))
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

    def nationwideRoute(field1: BrowserField, field2: BrowserField): Route = {
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(
              browserService
                .fetchData(field1, field2))
          )
        )
      } ~ (path("pipe") & get) {
        complete(
          HttpEntity(
            `text/plain(UTF-8)`,
            pipeSource(
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

    def msaRoute(msaMd: MsaMd,
                 field1: BrowserField,
                 field2: BrowserField): Route = {
      (path("csv") & get) {
        complete(
          HttpEntity(
            `text/csv(UTF-8)`,
            csvSource(browserService.fetchData(msaMd, field1, field2))
          )
        )
      } ~ (path("pipe") & get) {
        complete(
          HttpEntity(
            `text/plain(UTF-8)`,
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

    pathPrefix("view") {
      (extractActions & extractRaces & extractSexes &
        extractLoanType & extractLoanPurpose & extractLienStatus &
        extractConstructionMethod) {
        (actionsTaken,
         races,
         sexes,
         loanTypes,
         loanPurposes,
         lienStatuses,
         constructionMethods) =>
          val filteredfields =
            List(actionsTaken,
                 races,
                 sexes,
                 loanTypes,
                 loanPurposes,
                 lienStatuses,
                 constructionMethods).filter(_.name != "empty")
          if (filteredfields.length > 2) {
            complete((BadRequest, "More than 2 fields provided"))
          } else if (filteredfields.length == 2) {
            val sortedFields = filteredfields.sortBy(_.name)
            val field1 = sortedFields.head
            val field2 = sortedFields.last
            // /data-browser-api/view/nationwide(/csv)
            pathPrefix("nationwide")(nationwideRoute(field1, field2)) ~
              // /data-browser-api/view/msamd/<msamd>(/csv)
              pathPrefix("msamd" / MsaMdSegment) { msaMd =>
                msaRoute(msaMd, field1, field2)
              } ~
              pathPrefix("state" / StateSegment) { state =>
                // /data-browser-api/view/state/<state>/msamd/<msamd>(/csv)
                pathPrefix("msamd" / MsaMdSegment) { msaMd =>
                  stateAndMsaRoute(state, msaMd, field1, field2)
                } ~
                  // /data-browser-api/view/state/<state>(/csv)
                  stateRoute(state, field1, field2)
              }
          } else if (filteredfields.length == 1) {
            val field1 = filteredfields.head
            val field2 = BrowserField()
            // /data-browser-api/view/nationwide(/csv)
            pathPrefix("nationwide")(nationwideRoute(field1, field2)) ~
              // /data-browser-api/view/msamd/<msamd>(/csv)
              pathPrefix("msamd" / MsaMdSegment) { msaMd =>
                msaRoute(msaMd, field1, field2)
              } ~
              pathPrefix("state" / StateSegment) { state =>
                // /data-browser-api/view/state/<state>/msamd/<msamd>(/csv)
                pathPrefix("msamd" / MsaMdSegment) { msaMd =>
                  stateAndMsaRoute(state, msaMd, field1, field2)
                } ~
                  // /data-browser-api/view/state/<state>(/csv)
                  stateRoute(state, field1, field2)
              }
          } else {
            val field1 = BrowserField()
            val field2 = BrowserField()
            // /data-browser-api/view/nationwide(/csv)
            pathPrefix("nationwide")(nationwideRoute(field1, field2)) ~
              // /data-browser-api/view/msamd/<msamd>(/csv)
              pathPrefix("msamd" / MsaMdSegment) { msaMd =>
                msaRoute(msaMd, field1, field2)
              } ~
              pathPrefix("state" / StateSegment) { state =>
                // /data-browser-api/view/state/<state>/msamd/<msamd>(/csv)
                pathPrefix("msamd" / MsaMdSegment) { msaMd =>
                  stateAndMsaRoute(state, msaMd, field1, field2)
                } ~
                  // /data-browser-api/view/state/<state>(/csv)
                  stateRoute(state, field1, field2)
              }
          }
      }
    }
  }
}
