package hmda.data.browser.rest

import akka.http.scaladsl.common.{
  CsvEntityStreamingSupport,
  EntityStreamingSupport
}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.models._
import hmda.data.browser.rest.DataBrowserDirectives._
import hmda.data.browser.services.BrowserService
import io.circe.generic.auto._
import monix.execution.{Scheduler => MonixScheduler}

object Routes {
  implicit val csvStreamingSupport: CsvEntityStreamingSupport =
    EntityStreamingSupport.csv()

  def apply(browserService: BrowserService)(implicit scheduler: MonixScheduler,
                                            mat: ActorMaterializer): Route = {
    pathPrefix("data-browser" / "view") {
      pathPrefix("msamd" / MsaMdSegment) { msamd =>
        (extractActions & extractRaces) {
          (actionsTaken: Seq[ActionTaken], races: Seq[Race]) =>
            // eg. data-browser/view/msamd/45636/csv?actions_taken=1,2,3&races=Asian,Joint,White
            (path("csv") & get) {
              complete(
                HttpEntity(
                  ContentTypes.`text/csv(UTF-8)`,
                  browserService
                    .fetchData(msamd, races, actionsTaken)
                    .map(_.toCsv)
                    .map(ByteString(_))
                    .via(csvStreamingSupport.framingRenderer)
                )
              )
            } ~
              // eg. data-browser/view/msamd/45636?actions_taken=1,2,3&races=Asian,Joint,White
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
      } ~
        pathPrefix("state" / StateSegment) { state: State =>
          (extractActions & extractRaces) { (actionsTaken, races) =>
            (path("csv") & get) {
              complete(
                HttpEntity(
                  ContentTypes.`text/csv(UTF-8)`,
                  browserService
                    .fetchData(state, races, actionsTaken)
                    .map(_.toCsv)
                    .map(ByteString(_))
                    .via(csvStreamingSupport.framingRenderer)
                )
              )
            } ~
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
        } ~
        pathPrefix("nationwide") {
          (extractActions & extractRaces) { (actionsTaken, races) =>
            (path("csv") & get) {
              complete(
                HttpEntity(
                  ContentTypes.`text/csv(UTF-8)`,
                  browserService
                    .fetchData(races, actionsTaken)
                    .map(_.toCsv)
                    .map(ByteString(_))
                    .via(csvStreamingSupport.framingRenderer)
                )
              )
            } ~
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
