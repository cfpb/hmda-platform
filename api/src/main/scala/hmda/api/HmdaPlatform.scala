package hmda.api

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.admin.InstitutionAdminHttpApi

import scala.concurrent.ExecutionContext

object HmdaPlatform extends InstitutionAdminHttpApi {

  val config = ConfigFactory.load()
  lazy val adminHost = config.getString("hmda.http.adminHost")
  lazy val adminPort = config.getInt("hmda.http.adminPort")

  def main(args: Array[String]): Unit = {
    HmdaApi.main(Array.empty)
    new HmdaAdminApi(HmdaApi.system).main(Array.empty)

    val http = Http().bindAndHandle(
      institutionAdminPath,
      adminHost,
      adminPort
    )

    http onFailure {
      case ex: Exception =>
        log.error(ex, "Failed to bind to {}:{}", adminHost, adminPort)
    }

  }

  override implicit val system: ActorSystem = ActorSystem("hmda")
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  override val log: LoggingAdapter = Logging(system, getClass)
}
