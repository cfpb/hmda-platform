package hmda.api.tcp.admin

import akka.NotUsed
import akka.pattern.pipe
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Tcp}
import akka.util.ByteString
import hmda.api.tcp.TcpApi
import hmda.api.util.FlowUtils

import scala.concurrent.{ExecutionContext, Future}

class InstitutionAdminTcpApi(supervisor: ActorRef) extends TcpApi with FlowUtils {
  override val name: String = "hmda-institutions-tcp-api"

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher

  override val host: String = config.getString("hmda.panel.tcp.host")
  override val port: Int = config.getInt("hmda.panel.tcp.port")


  val tcpHandler: Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString]
    .map{ e => println(e); e}


  override val tcp: Future[Tcp.ServerBinding] = Tcp().bindAndHandle(
    tcpHandler,
    host,
    port
  )

  tcp pipeTo self
}
