package hmda.data.browser

 import akka.actor.typed.ActorSystem
import hmda.data.browser.ServerGuardian.Protocol

 object ruWebServer extends App {
  val guardian = ActorSystem[Protocol](ServerGuardian.behavior, "service")
}