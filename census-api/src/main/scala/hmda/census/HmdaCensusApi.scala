package hmda.census

import akka.actor.ActorSystem
import org.slf4j.LoggerFactory

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
  implicit val system = ActorSystem("hmda-census")
  system.actorOf(HmdaCensusGrpc.props(), name = "hmda-census-grpc")
}
