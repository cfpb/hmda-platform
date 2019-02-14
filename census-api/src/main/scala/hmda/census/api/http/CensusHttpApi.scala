package hmda.census.api.http

import akka.NotUsed
import akka.actor.ActorSystem

import scala.util.{Failure, Success}
import akka.event.LoggingAdapter
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.stream.scaladsl.Source
import hmda.census.validation.CensusValidation._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.census.dtos.IndexedCensusEntry
import hmda.census.records.CensusRecords
import hmda.census.records.CensusRecords._
import hmda.model.census.Census
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.Try
import hmda.util.http.FilingResponseUtils.failedResponse
trait CensusHttpApi extends HmdaTimeDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  def streamCensusRecords(
                           input: Map[String, Census],
                           inputType: String): Source[IndexedCensusEntry, NotUsed] =
    Source
      .fromIterator(() => input.toIterator)
      .map {
        case (index, census) => IndexedCensusEntry(index, census, inputType)
      }

  val censusHttpRoutes =
    encodeResponse {

      pathPrefix("streaming") {
        path("tract") {
          get {
            complete(streamCensusRecords(CensusRecords.indexedTract, "tract"))
          }
        } ~
          path("county") {
            get {
              complete(
                streamCensusRecords(CensusRecords.indexedCounty, "county"))
            }
          } ~
          path("smallcounty") {
            get {
              complete(streamCensusRecords(CensusRecords.indexedSmallCounty,
                "smallcounty"))
            }
          }
      } ~
      pathPrefix("census" / "tract" / Segment) { tract =>
        extractUri { uri =>
          val response = CensusRecords.indexedTract.getOrElse(tract, Census())
          complete(response)
        }
      } ~
        pathPrefix("census" / "validate") {
          path("tract") {
            extractUri { uri =>
              entity(as[TractCheck]) { tc =>
                val tract = tc.tract
                complete(TractValidated(isTractValid(tract, indexedTract)))
              }
            }
          } ~
            path("county") {
              extractUri { uri =>
                entity(as[CountyCheck]) { tc =>
                  val county = tc.county
                  complete(TractValidated(isCountyValid(county, indexedCounty)))
                }
              }
            } ~
            path("smallcounty") {
              extractUri { uri =>
                entity(as[CountyCheck]) { tc =>
                  val county = tc.county
                  complete(TractValidated(isCountySmall(county, indexedSmallCounty)))
                }
              }
            }
        }
    }
}
