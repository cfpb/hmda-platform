package hmda.rateLimit

import akka.actor.{ ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.{ Http, HttpConnectionContext }
import akka.pattern.pipe
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import hmda.api.HmdaServer
import hmda.rateLimit.api.grpc.RateLimitServiceImpl
import pb.lyft.ratelimit._

import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.concurrent.{ ExecutionContext, Future }

object RateLimitApi {
  def props(): Props = Props(new RateLimitApi)
}

class RateLimitApi extends HmdaServer {

  override implicit val system: ActorSystem        = context.system
  override implicit val materializer: Materializer = Materializer(context)
  override implicit val ec: ExecutionContext       = context.dispatcher
  override val log                                 = Logging(system, getClass)

  val config: Config = ConfigFactory.load()

  val limit = config.getInt("hmda.rateLimit.limit")
  val duration: FiniteDuration =
    config.getInt("hmda.rateLimit.grpc.timeout").seconds

  implicit val timeout: Timeout = Timeout(duration)

  override val name: String = "rate-limit-api"
  override val host: String = config.getString("hmda.rateLimit.grpc.host")
  override val port: Int    = config.getInt("hmda.rateLimit.grpc.port")

  val service: HttpRequest => Future[HttpResponse] =
    RateLimitServiceHandler(new RateLimitServiceImpl(limit))

  override val http: Future[Http.ServerBinding] =
    Http(system).bindAndHandleAsync(
      service,
      host,
      port,
      connectionContext = HttpConnectionContext()
    )

  http pipeTo self

}