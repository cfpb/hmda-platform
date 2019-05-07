package hmda.ratelimit

import akka.actor.ActorSystem
import hmda.rateLimit.api._
import org.slf4j.LoggerFactory

object RateLimit extends App {

  val log = LoggerFactory.getLogger("rate-limit")

  log.info("""
      | _____                       _   _                              _
      ||  __ \                     | | (_)                 /\         (_)
      || |__) |___ _ __   ___  _ __| |_ _ _ __   __ _     /  \   _ __  _
      ||  _  // _ \ '_ \ / _ \| '__| __| | '_ \ / _` |   / /\ \ | '_ \| |
      || | \ \  __/ |_) | (_) | |  | |_| | | | | (_| |  / ____ \| |_) | |
      ||_|  \_\___| .__/ \___/|_|   \__|_|_| |_|\__, | /_/    \_\ .__/|_|
      |           | |                            __/ |          | |
      |           |_|                           |___/           |_|
      |
    """.stripMargin)
  implicit val system: ActorSystem = ActorSystem("rate-limit")
  system.actorOf(RateLimitApi.props(), "rate-limit")
}
