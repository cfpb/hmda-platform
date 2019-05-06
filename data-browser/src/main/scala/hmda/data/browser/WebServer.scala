package hmda.data.browser

import akka.actor.typed.ActorSystem
import hmda.data.browser.ServerGuardian.Protocol

object WebServer extends App {
  val guardian = ActorSystem[Protocol](ServerGuardian.behavior, "service")
}
