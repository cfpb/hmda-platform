package hmda.census.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.census.model.CensusModel.{CountyCheck, TractCheck, TractValidated}
import hmda.census.records.CensusRecords
import hmda.census.records.CensusRecords._
import hmda.census.validation.CensusValidation._
import hmda.model.census.Census
import hmda.util.http.FilingResponseUtils.failedResponse
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}
trait CensusHttpApi extends HmdaTimeDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  val censusHttpRoutes =
    encodeResponse {
      pathPrefix("census" / "tract" / Segment) { tract =>
        extractUri { uri =>
          val census = CensusRecords.indexedTract.get(tract)
          census match {
            case Some(t) => complete(ToResponseMarshallable(t))
            case _       => complete(ToResponseMarshallable(Census()))
          }
        }
      } ~
        pathPrefix("census" / "validate") {
          path("tract") {
            extractUri { uri =>
              entity(as[TractCheck]) { tc =>
                val tract = tc.tract
                val isValid = Try(isTractValid(tract, indexedTract))
                isValid match {
                  case Success(value) =>
                    val c = CensusRecords
                    val validated = TractValidated(value)
                    complete(ToResponseMarshallable(validated))
                  case Failure(error) =>
                    failedResponse(StatusCodes.BadRequest, uri, error)
                }
              }
            }
          } ~
            path("county") {
              extractUri { uri =>
                entity(as[CountyCheck]) { tc =>
                  val county = tc.county
                  val isValid = Try(isCountyValid(county, indexedCounty))
                  isValid match {
                    case Success(value) =>
                      val c = CensusRecords
                      val validated = TractValidated(value)
                      complete(ToResponseMarshallable(validated))
                    case Failure(error) =>
                      failedResponse(StatusCodes.BadRequest, uri, error)
                  }
                }
              }
            } ~
            path("smallcounty") {
              extractUri { uri =>
                entity(as[CountyCheck]) { tc =>
                  val county = tc.county
                  val isValid = Try(isCountySmall(county, indexedSmallCounty))
                  isValid match {
                    case Success(value) =>
                      val c = CensusRecords
                      val validated = TractValidated(value)
                      complete(ToResponseMarshallable(validated))
                    case Failure(error) =>
                      failedResponse(StatusCodes.BadRequest, uri, error)
                  }
                }
              }
            }
        }
    }
}
