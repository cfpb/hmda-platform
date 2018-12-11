package hmda.census

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import hmda.census.api.http.HmdaCensusQueryApi
//import hmda.institution.api.http.HmdaInstitutionQueryApi
import org.slf4j.LoggerFactory
import akka.actor.typed.scaladsl.adapter._
//import hmda.institution.projection.InstitutionDBProjection
import hmda.messages.projection.CommonProjectionMessages.StartStreaming
//import main.scala.hmda.institution.api.http.HmdaInstitutionQueryApi

object HmdaCensusApi extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
      |   ____                              _          _
      |  / ___|___ _ __  ___ _   _ ___     / \   _ __ (_)
      | | |   / _ \ '_ \/ __| | | / __|   / _ \ | '_ \| |
      | | |__|  __/ | | \__ \ |_| \__ \  / ___ \| |_) | |
      |  \____\___|_| |_|___/\__,_|___/ /_/   \_\ .__/|_|
      |                                         |_|
    """.stripMargin)
  val config = ConfigFactory.load()
  val host = config.getString("hmda.census.http.host")
  val port = config.getString("hmda.census.http.port")
  val jdbcUrl = config.getString("db.db.url")
  log.info(s"Connection URL is \n\n$jdbcUrl\n")
  implicit val system = ActorSystem("hmda-census")
  system.actorOf(HmdaCensusQueryApi.props(), "hmda-census-api")
  system.actorOf(HmdaCensusGrpc.props(), name = "hmda-census-grpc")
}
