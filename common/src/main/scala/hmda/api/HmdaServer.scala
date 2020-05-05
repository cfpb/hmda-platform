package hmda.api

import java.net.InetSocketAddress

import akka.actor.{ Actor, ActorSystem, Status }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.Materializer
import hmda.actor.HmdaActor

import scala.concurrent.{ ExecutionContext, Future }

trait HmdaServer extends HmdaActor {
  val name: String
  val host: String
  val port: Int

  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val ec: ExecutionContext

  val http: Future[ServerBinding]

  override def receive: Receive = {
    case Http.ServerBinding(s) => handleServerBinding(s)
    case Status.Failure(e)     => handleBindFailure(e)
  }

  private def handleServerBinding(address: InetSocketAddress) = {
    log.info(s"$name started on {}", address)
    context.become(Actor.emptyBehavior)
  }

  private def handleBindFailure(error: Throwable) = {
    log.error(error, s"Failed to bind to $host:$port")
    context stop self
  }

}