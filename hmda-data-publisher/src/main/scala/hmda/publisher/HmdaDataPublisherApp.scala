package hmda.publisher

import akka.actor.{ ActorSystem, Props }
import com.typesafe.config.{ ConfigFactory, ConfigValueFactory }
import hmda.publisher.scheduler._
import org.slf4j.LoggerFactory

// $COVERAGE-OFF$
object HmdaDataPublisherApp extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      | _    _ __  __ _____            _____        _          _____       _     _ _     _
      || |  | |  \/  |  __ \   /\     |  __ \      | |        |  __ \     | |   | (_)   | |
      || |__| | \  / | |  | | /  \    | |  | | __ _| |_ __ _  | |__) |   _| |__ | |_ ___| |__   ___ _ __
      ||  __  | |\/| | |  | |/ /\ \   | |  | |/ _` | __/ _` | |  ___/ | | | '_ \| | / __| '_ \ / _ \ '__|
      || |  | | |  | | |__| / ____ \  | |__| | (_| | || (_| | | |   | |_| | |_) | | \__ \ | | |  __/ |
      ||_|  |_|_|  |_|_____/_/    \_\ |_____/ \__,_|\__\__,_| |_|    \__,_|_.__/|_|_|___/_| |_|\___|_|                                             |___/
    """.stripMargin
  )

  val config = ConfigFactory.load()

  val panelTimer2018 = config.getString("akka.PanelScheduler2018")
  val larTimer2018   = config.getString("akka.LarScheduler2018")
  val tsTimer2018    = config.getString("akka.TsScheduler2018")

  val panelTimer2019        = config.getString("akka.PanelScheduler2019").split(",")
  val larTimer2019          = config.getString("akka.LarScheduler2019").split(",")
  val larTimerLoanLimit2019 = config.getString("akka.LarSchedulerLoanLimit2019").split(",")

  val tsTimer2019 = config.getString("akka.TsScheduler2019").split(",")

  val larPublicTimer2018 = config.getString("akka.LarPublicScheduler2018")
  val tsPublicTimer2018  = config.getString("akka.TsPublicScheduler2018")

  val larTimerQuarterly2020 = config.getString("akka.LarSchedulerQuarterly2020").split(",")
  val tsTimerQuarterly2020  = config.getString("akka.TsSchedulerQuarterly2020").split(",")

  log.info("Panel Timer 2018: " + panelTimer2018)
  log.info("LAR Timer 2018: " + larTimer2018)
  log.info("TS Timer 2018: " + tsTimer2018)

  log.info("Panel Timer 2019: " + panelTimer2019)
  log.info("LAR Timer 2019: " + larTimer2019)
  log.info("LAR Timer 2019 LoanLimit: " + larTimerLoanLimit2019)

  log.info("TS Timer 2019: " + tsTimer2019)

  log.info("LAR Public 2018: " + larPublicTimer2018)
  log.info("TS Public 2018: " + tsPublicTimer2018)

  log.info("LAR Quarterly 2020: " + larTimerQuarterly2020)
  log.info("TS Quarterly 2020: " + tsTimerQuarterly2020)

  val panelActorSystem =
    ActorSystem(
      "panelTask",
      ConfigFactory
        .parseString(panelTimer2018)
        .withValue(panelTimer2019(0), ConfigValueFactory.fromAnyRef(panelTimer2019(1)))
        .withFallback(config)
    )
  panelActorSystem.actorOf(Props[PanelScheduler], "PanelScheduler")

  val larActorSystem =
    ActorSystem(
      "larTask",
      ConfigFactory
        .parseString(larTimer2018)
        .withValue(larTimer2019(0), ConfigValueFactory.fromAnyRef(larTimer2019(1)))
        .withValue(larTimerLoanLimit2019(0), ConfigValueFactory.fromAnyRef(larTimerLoanLimit2019(1)))
        .withValue(larTimerQuarterly2020(0), ConfigValueFactory.fromAnyRef(larTimerQuarterly2020(1)))
        .withFallback(config)
    )
  larActorSystem.actorOf(Props[LarScheduler], "LarScheduler")

  val tsActorSystem =
    ActorSystem(
      "tsTask",
      ConfigFactory
        .parseString(tsTimer2018)
        .withValue(tsTimer2019(0), ConfigValueFactory.fromAnyRef(tsTimer2019(1)))
        .withValue(tsTimerQuarterly2020(0), ConfigValueFactory.fromAnyRef(tsTimerQuarterly2020(1)))
        .withFallback(config)
    )
  tsActorSystem.actorOf(Props[TsScheduler], "TsScheduler")

  val larPublicActorSystem =
    ActorSystem("larPublicTask", ConfigFactory.parseString(larPublicTimer2018).withFallback(config))

  larPublicActorSystem.actorOf(Props[LarPublicScheduler], "LarPublicScheduler")

  val tsPublicActorSystem =
    ActorSystem("tsPublicTask", ConfigFactory.parseString(tsPublicTimer2018).withFallback(config))

  tsPublicActorSystem.actorOf(Props[TsPublicScheduler], "TsPublicScheduler")

}
// $COVERAGE-ON$