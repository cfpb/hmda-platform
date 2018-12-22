package hmda.regulator

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import hmda.regulator.scheduler.{LarScheduler, PanelScheduler, TsScheduler}
import org.slf4j.LoggerFactory
object HmdaRegulatorApp extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      | _    _ __  __ _____            _____                  _       _
      || |  | |  \/  |  __ \   /\     |  __ \                | |     | |
      || |__| | \  / | |  | | /  \    | |__) |___  __ _ _   _| | __ _| |_ ___  _ __
      ||  __  | |\/| | |  | |/ /\ \   |  _  // _ \/ _` | | | | |/ _` | __/ _ \| '__|
      || |  | | |  | | |__| / ____ \  | | \ \  __/ (_| | |_| | | (_| | || (_) | |
      ||_|  |_|_|  |_|_____/_/    \_\ |_|  \_\___|\__, |\__,_|_|\__,_|\__\___/|_|
      |                                            __/ |
      |                                           |___/
    """.stripMargin)

  val config = ConfigFactory.load()

  val host = config.getString("hmda.regulator.http.host")
  val port = config.getString("hmda.regulator.http.port")

  val panelTimer = config.getString("akka.PanelScheduler")
  val larTimer = config.getString("akka.LarScheduler")
  val tsTimer = config.getString("akka.TsScheduler")
//
//  log.info(s"Panel Schedule \n\n${panelTimer}\n")
//
//  log.info(s"LAR Schedule \n\n${larTimer}\n")
//
//  log.info(s"TS Schedule \n\n${tsTimer}\n")

  val panelActorSystem =
    ActorSystem("panelTask",
                ConfigFactory.parseString(panelTimer).withFallback(config))
  panelActorSystem.actorOf(Props[PanelScheduler], "PanelScheduler")

//  val larActorSystem =
//    ActorSystem("larTask", ConfigFactory.parseString(larTimer))
//  larActorSystem.actorOf(Props[LarScheduler], "LarScheduler")
//
  val tsActorSystem = ActorSystem("tsTask", ConfigFactory.parseString(tsTimer))
  tsActorSystem.actorOf(Props[TsScheduler], "TsScheduler")

//  Thread.sleep(60000)
//
//  panelActorSystem.terminate()
//  larActorSystem.terminate()
//  tsActorSystem.terminate()

}
