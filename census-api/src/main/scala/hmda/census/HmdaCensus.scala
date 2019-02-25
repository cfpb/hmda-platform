package hmda.census

import akka.actor.ActorSystem
import hmda.census.api.http.HmdaCensusApi
import org.slf4j.LoggerFactory

object HmdaCensus extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
      |   ____                              _          _
      |  / ___|___ _ __  ___ _   _ ___     / \   _ __ (_)
      | | |   / _ \ '_ \/ __| | | / __|   / _ \ | '_ \| |
      | | |__|  __/ | | \__ \ |_| \__ \  / ___ \| |_) | |
      |  \____\___|_| |_|___/\__,_|___/ /_/   \_\ .__/|_|
      |                                         |_|
    """.stripMargin)

  implicit val system: ActorSystem = ActorSystem("hmda-census")
  system.actorOf(HmdaCensusApi.props(), "hmda-census-api")
}
