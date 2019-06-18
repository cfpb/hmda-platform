package hmda.regulator

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import hmda.regulator.scheduler._
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

  val panelTimer2018 = config.getString("akka.PanelScheduler2018")
  val larTimer2018 = config.getString("akka.LarScheduler2018")
  val tsTimer2018 = config.getString("akka.TsScheduler2018")

  val panelTimer2019 = config.getString("akka.PanelScheduler2019").split(",")
  val larTimer2019 = config.getString("akka.LarScheduler2019").split(",")
  val tsTimer2019 = config.getString("akka.TsScheduler2019").split(",")

  log.info("Panel Timer 2018: " + panelTimer2018)
  log.info("larTimer 2018: " + larTimer2018)
  log.info("tsTimer 2018: " + tsTimer2018)

  log.info("Panel Timer 2019: " + panelTimer2019)
  log.info("larTimer 2019: " + larTimer2019)
  log.info("tsTimer 2019: " + tsTimer2019)

  val panelActorSystem =
    ActorSystem(
      "panelTask",
      ConfigFactory
        .parseString(panelTimer2018)
        .withValue(panelTimer2019(0),ConfigValueFactory.fromAnyRef(panelTimer2019(1)))
        .withFallback(config)
    )
  panelActorSystem.actorOf(Props[PanelScheduler], "PanelScheduler")

//  val larActorSystem =
//    ActorSystem("larTask2018", ConfigFactory.parseString(larTimer2018))
//  larActorSystem.actorOf(Props[LarScheduler2018], "LarScheduler2018")
//
//
//
//  val tsActorSystem = ActorSystem("tsTask2018", ConfigFactory.parseString(tsTimer2018))
//  tsActorSystem.actorOf(Props[TsScheduler2018], "TsScheduler2018")

}
