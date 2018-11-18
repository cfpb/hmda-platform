package hmda.api.http

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.filing.FilingHttpApi
import hmda.api.http.filing.InstitutionHttpApi
import hmda.api.http.routes.BaseHttpApi
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.actor.typed.scaladsl.adapter._
import hmda.api.http.filing.submissions._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object HmdaFilingApi {
  def props: Props = Props(new HmdaFilingApi)
  final val filingApiName = "hmda-filing-api"
}

class HmdaFilingApi
    extends HttpServer
    with BaseHttpApi
    with FilingHttpApi
    with SubmissionHttpApi
    with UploadHttpApi
    with ParseErrorHttpApi
    with InstitutionHttpApi
    with VerifyHttpApi
    with SignHttpApi
    with EdtisHttpApi {
  import HmdaFilingApi._

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  val timeout: Timeout = Timeout(config.getInt("hmda.http.timeout").seconds)
  override val log = Logging(system, getClass)

  val sharding = ClusterSharding(system.toTyped)

  override val name: String = filingApiName
  override val host: String = config.getString("hmda.http.filingHost")
  override val port: Int = config.getInt("hmda.http.filingPort")

  override val paths
    : Route = routes(s"$name") ~ filingRoutes ~ submissionRoutes ~ uploadRoutes ~ institutionRoutes ~ parserErrorRoute ~ verifyRoutes ~ signRoutes ~ editsRoutes

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
