package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

trait PublicHttpApi extends PublicLarHttpApi with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  val publicHttpRoutes =
    extractExecutionContext { executor =>
      implicit val ec: ExecutionContext = executor
      encodeResponse {
        pathPrefix("institutions" / Segment) { instId =>
          modifiedLar(instId)
        }
      }
    }
}
