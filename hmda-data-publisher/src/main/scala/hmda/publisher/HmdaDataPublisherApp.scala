package hmda.publisher

import akka.actor.{ActorSystem, Props}
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

  val actorSystem = ActorSystem("hmda-data-publisher")
  val config      = actorSystem.settings.config
  config.getObject("akka.quartz.schedules").forEach((k, v) => log.info(s"$k = ${v.render()}"))

  actorSystem.actorOf(Props[PanelScheduler], "PanelScheduler")
  actorSystem.actorOf(Props[LarScheduler], "LarScheduler")
  actorSystem.actorOf(Props[TsScheduler], "TsScheduler")
  actorSystem.actorOf(Props[LarPublicScheduler], "LarPublicScheduler")
  actorSystem.actorOf(Props[TsPublicScheduler], "TsPublicScheduler")

}
// $COVERAGE-ON$