package hmda.api.http.public

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.public.PublicProtocol
import akka.http.scaladsl.server.Directives._
import hmda.api.http.public.lar.PublicLarHttpApi

import scala.concurrent.ExecutionContext

trait PublicHttpApi extends PublicProtocol with PublicLarHttpApi with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  val publicHttpRoutes =
    extractExecutionContext { executor =>
      implicit val ec: ExecutionContext = executor
      encodeResponse {
        modifiedLar
      }
    }
}
