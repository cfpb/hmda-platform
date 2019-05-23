package hmda.rateLimit

import akka.actor.ActorSystem
import org.slf4j.LoggerFactory

object RateLimitApp extends App {

  val log = LoggerFactory.getLogger("rate-limit")

  log.info("""
      |  ____       _         _     _           _ _   
      | |  _ \ __ _| |_ ___  | |   (_)_ __ ___ (_) |_ 
      | | |_) / _` | __/ _ \ | |   | | '_ ` _ \| | __|
      | |  _ < (_| | ||  __/ | |___| | | | | | | | |_ 
      | |_| \_\__,_|\__\___| |_____|_|_| |_| |_|_|\__|
      |                                        
      |
    """.stripMargin)
  implicit val system: ActorSystem = ActorSystem("rate-limit")
  system.actorOf(RateLimitApi.props(), "rate-limit")
}
