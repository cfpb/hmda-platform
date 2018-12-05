package hmda.uli

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import hmda.uli.api.http.HmdaUliApi
import org.slf4j.LoggerFactory

object HmdaUli extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
    | _    _ __  __ _____            _    _ _      _____
    | | |  | |  \/  |  __ \   /\     | |  | | |    |_   _|
    | | |__| | \  / | |  | | /  \    | |  | | |      | |
    | |  __  | |\/| | |  | |/ /\ \   | |  | | |      | |
    | | |  | | |  | | |__| / ____ \  | |__| | |____ _| |_
    | |_|  |_|_|  |_|_____/_/    \_\  \____/|______|_____|
  """.stripMargin)

  val config = ConfigFactory.load()

  val host = config.getString("hmda.uli.http.host")
  val port = config.getInt("hmda.uli.http.port")

  implicit val system = ActorSystem("hmda-uli")

  system.actorOf(HmdaUliApi.props(), "hmda-uli-api")
  system.actorOf(HmdaUliGrpc.props(), name = "hmda-uli-grpc")
}
