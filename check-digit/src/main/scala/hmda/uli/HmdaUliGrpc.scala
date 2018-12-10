package hmda.uli

import akka.actor.{ActorSystem, Props}
import akka.pattern.pipe
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.grpc.GrpcServer
import hmda.grpc.services.CheckDigitServiceHandler
import hmda.uli.api.grpc.CheckDigitServiceImpl

import scala.concurrent.{ExecutionContext, Future}

object HmdaUliGrpc {
  def props(): Props = Props(new HmdaUliGrpc)
}

class HmdaUliGrpc extends GrpcServer {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher

  override val name: String = "hmda-uli-grpc"

  override val host: String = config.getString("hmda.uli.grpc.host")
  override val port: Int = config.getInt("hmda.uli.grpc.port")

  override val service: HttpRequest => Future[HttpResponse] =
    CheckDigitServiceHandler(new CheckDigitServiceImpl(materializer))

  override val http: Future[Http.ServerBinding] = Http().bindAndHandleAsync(
    service,
    interface = host,
    port = port,
    connectionContext = HttpConnectionContext(http2 = Always)
  )

  http pipeTo self
}
