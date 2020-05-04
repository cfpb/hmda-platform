package hmda.uli

import akka.actor.typed.ActorSystem
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

  ActorSystem[Nothing](HmdaUliApi(), HmdaUliApi.name)
}