package hmda.api.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Status}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp
import akka.stream.scaladsl.Tcp.ServerBinding
import hmda.persistence.model.HmdaActor

import scala.concurrent.{ExecutionContext, Future}

abstract class TcpApi extends HmdaActor {

  val name: String
  val host: String
  val port: Int

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  val tcp: Future[ServerBinding]

  override def receive = {
    case Tcp.ServerBinding(s) => handleServerBinding(s)
    case Status.Failure(e) => handleBindFailure(e)
  }

  private def handleServerBinding(address: InetSocketAddress): Unit = {
    log.info(s"$name started on {}", address)
    context.become(Actor.emptyBehavior)
  }

  private def handleBindFailure(error: Throwable): Unit = {
    log.error(error, s"Failed to bind to $host:$port")
    context stop self
  }
}
