package hmda.census.api.http

import akka.actor.ActorSystem

import scala.util.{Failure, Success}
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import hmda.census.model.CensusModel.{CountyCheck, TractCheck, TractValidated}
import hmda.census.validation.CensusValidation._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.census.records.CensusRecords
import hmda.census.records.CensusRecords._
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

  val censusHttpRoutes =
    encodeResponse {
      pathPrefix("census" / "tract" / Segment) { tract =>
        timedGet { uri =>
          val census = CensusRecords.indexedTract.get(tract)
          census match {
            case Some(t) => complete(t)
            case _ =>
              failedResponse(StatusCodes.NotFound,
                             uri,
                             new Exception("Tract not found"))
          }
        }
      } ~
        pathPrefix("census" / "validate") {
          path("tract") {
            timedPost { uri =>
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
              timedPost { uri =>
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
              timedPost { uri =>
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
