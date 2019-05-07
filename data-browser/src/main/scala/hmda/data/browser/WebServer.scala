package hmda.data.browser

import akka.actor.typed.ActorSystem
import hmda.data.browser.ApplicationGuardian.Protocol

object WebServer extends App {
  val guardian = ActorSystem[Protocol](ApplicationGuardian.behavior, "service")
}
