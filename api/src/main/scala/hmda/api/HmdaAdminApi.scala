package hmda.api

import akka.actor.{ ActorSystem, Props, ActorRef }
import akka.event.Logging
import akka.pattern.pipe
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.BaseHttpApi
import hmda.api.http.admin.InstitutionAdminHttpApi
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object HmdaAdminApi {
  def props(supervisor: ActorRef): Props = Props(new HmdaAdminApi(supervisor))
}

class HmdaAdminApi(supervisor: ActorRef) extends HttpApi with BaseHttpApi with InstitutionAdminHttpApi {

  val config = ConfigFactory.load()

  lazy val httpTimeout = config.getInt("hmda.http.timeout")
  override implicit val timeout = Timeout(httpTimeout.seconds)

  override val name = "hmda-admin-api"

  override val host: String = config.getString("hmda.http.adminHost")
  override val port: Int = config.getInt("hmda.http.adminPort")

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  override val paths: Route = routes(s"$name") ~ institutionAdminRoutes(supervisor)

  override val http: Future[ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self
}
