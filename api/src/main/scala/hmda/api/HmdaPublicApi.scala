package hmda.api

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.Logging
import akka.pattern.{ pipe, ask }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.BaseHttpApi
import hmda.api.http.public.{ InstitutionSearchPaths, PublicHttpApi }
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.view.institutions.InstitutionView
import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object HmdaPublicApi {
  def props(querySupervisor: ActorRef): Props = Props(new HmdaPublicApi(querySupervisor))
}

class HmdaPublicApi(querySupervisor: ActorRef)
    extends HttpApi
    with BaseHttpApi
    with InstitutionSearchPaths
    with PublicHttpApi {

  val configuration = ConfigFactory.load()

  lazy val httpTimeout = configuration.getInt("hmda.http.timeout")
  override implicit val timeout = Timeout(httpTimeout.seconds)

  override val name: String = "hmda-public-api"
  override val host: String = configuration.getString("hmda.http.publicHost")
  override val port: Int = configuration.getInt("hmda.http.publicPort")

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val institutionViewF = (querySupervisor ? FindActorByName(InstitutionView.name))
    .mapTo[ActorRef]

  override val paths: Route = routes(s"$name") ~ institutionSearchPath(institutionViewF) ~ publicHttpRoutes

  override val http: Future[ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
