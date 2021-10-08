package hmda.uli

import akka.actor.typed.ActorSystem
import hmda.uli.api.http.HmdaUliApi
import org.slf4j.LoggerFactory

object HmdaUli extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
             |  _____ _               _      _____  _       _ _
             | / ____| |             | |    |  __ \(_)     (_) |
             || |    | |__   ___  ___| | __ | |  | |_  __ _ _| |_
             || |    | '_ \ / _ \/ __| |/ / | |  | | |/ _` | | __|
             || |____| | | |  __/ (__|   <  | |__| | | (_| | | |_
             | \_____|_| |_|\___|\___|_|\_\ |_____/|_|\__, |_|\__|
             |                                         __/ |
             |                                        |___/
  """.stripMargin)

  ActorSystem[Nothing](HmdaUliApi(), HmdaUliApi.name)
}