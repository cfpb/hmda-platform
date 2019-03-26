package hmda.calculator

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import hmda.calculator.api.http.RateSpreadAPI
import hmda.calculator.scheduler.APORScheduler
import org.slf4j.LoggerFactory

object HmdaRateSpread extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
             | _____         _______ ______  _____ _____  _____  ______          _____
             ||  __ \     /\|__   __|  ____|/ ____|  __ \|  __ \|  ____|   /\   |  __ \
             || |__) |   /  \  | |  | |__  | (___ | |__) | |__) | |__     /  \  | |  | |
             ||  _  /   / /\ \ | |  |  __|  \___ \|  ___/|  _  /|  __|   / /\ \ | |  | |
             || | \ \  / ____ \| |  | |____ ____) | |    | | \ \| |____ / ____ \| |__| |
             ||_|  \_\/_/    \_\_|  |______|_____/|_|    |_|  \_\______/_/    \_\_____/                                      |_|
           """.stripMargin)

  val config = ConfigFactory.load()

  val host = config.getString("hmda.ratespread.http.host")
  val port = config.getInt("hmda.ratespread.http.port")

  val aporUpdateTimer = config.getString("akka.APORScheduler")

  implicit val rateSpreadSystem: ActorSystem = ActorSystem(
    "ratespread-api-task")
  rateSpreadSystem.actorOf(RateSpreadAPI.props(), "hmda-ratespread-api")

  val aporUpdaterActorSystem =
    ActorSystem("aporTask", ConfigFactory.parseString(aporUpdateTimer))
  aporUpdaterActorSystem.actorOf(Props[APORScheduler], "APORScheduler")

}
