package hmda.census

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.grpc.GrpcServer
import hmda.api.grpc.GrpcServer
import hmda.census.api.grpc.CensusServiceImpl
import hmda.grpc.services.CensusServiceHandler

import scala.concurrent.{ExecutionContext, Future}
object HmdaCensusGrpc {
  def props(): Props = Props(new HmdaCensusGrpc)
}
class HmdaCensusGrpc extends GrpcServer {
  val config = ConfigFactory.load()
  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val name: String = "hmda-census-grpc"
  override val host: String = config.getString("hmda.census.grpc.host")
  override val port: Int = config.getInt("hmda.census.grpc.port")
  override val service: HttpRequest => Future[HttpResponse] =
    CensusServiceHandler(new CensusServiceImpl(materializer))
  override val http: Future[Http.ServerBinding] = Http().bindAndHandleAsync(
    service,
    interface = host,
    port = port,
    connectionContext = HttpConnectionContext(http2 = Always)
  )
  http pipeTo self
}
