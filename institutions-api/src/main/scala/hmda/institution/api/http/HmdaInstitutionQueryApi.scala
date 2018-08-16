package hmda.institution.api.http

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.HttpServer
import hmda.api.http.routes.BaseHttpApi

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object HmdaInstitutionQueryApi {
  def props(): Props = Props(new HmdaInstitutionQueryApi)
}

class HmdaInstitutionQueryApi
    extends HttpServer
    with BaseHttpApi
    with InstitutionQueryHttpApi {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val duration = config.getInt("hmda.institution.http.timeout").seconds

  override implicit val timeout: Timeout = Timeout(duration)

  val createSchema = config.getBoolean("hmda.institution.createSchema")

  if (createSchema) {
    institutionRepository.createSchema()
    institutionEmailsRepository.createSchema()
  }

  override val name: String = "hmda-institution-api"
  override val host: String = config.getString("hmda.institution.http.host")
  override val port: Int = config.getInt("hmda.institution.http.port")

  override val paths: Route = routes(s"$name") ~ institutionPublicRoutes

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
