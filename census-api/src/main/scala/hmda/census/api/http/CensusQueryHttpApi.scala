package hmda.census.api.http

import akka.actor.ActorSystem
import hmda.census.query.CensusComponent
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.model.census.Census
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

trait CensusQueryHttpApi extends HmdaTimeDirectives with CensusComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  def censusReadPath(indexedTract: Map[String, Census],
                     indexedCounty: Map[String, Census]) = {
//    println("censuses size: " + censuses.size)
    path("census" / IntNumber) { year =>
      timedGet { uri =>
//        val f = findByCollectionYr(year)
//        onComplete(f) {
//          case Success(censuses) =>
        complete(ToResponseMarshallable(indexedTract))
//        }
      }
    }
  }

}
