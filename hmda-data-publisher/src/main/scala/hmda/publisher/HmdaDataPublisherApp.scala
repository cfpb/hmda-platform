package hmda.publisher

import akka.actor.{ActorSystem, Props}
import hmda.publisher.helper.PGTableNameLoader
import hmda.publisher.scheduler._
import org.slf4j.LoggerFactory

// $COVERAGE-OFF$
object HmdaDataPublisherApp extends App with PGTableNameLoader {

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

  log.info("Using LAR 2018 Table: "+ lar2018TableName +"\n")
  log.info("Using MLAR 2018 Table: "+ mlar2018TableName+"\n")
  log.info("Using PANEl 2018 Table: "+ panel2018TableName+"\n")
  log.info("Using TS 2018 Table: "+ ts2018TableName+"\n")
  log.info("Using LAR 2019 Table: "+ lar2019TableName +"\n")
  log.info("Using MLAR 2019 Table: "+ mlar2019TableName+"\n")
  log.info("Using PANEl 2019 Table: "+ panel2019TableName+"\n")
  log.info("Using TS 2019 Table: "+ ts2019TableName+"\n")
  log.info("Using LAR 2020 Table: "+ lar2020TableName +"\n")
  log.info("Using MLAR 2020 Table: "+ mlar2020TableName+"\n")
  log.info("Using PANEl 2020 Table: "+ panel2020TableName+"\n")
  log.info("Using TS 2020 Table: "+ ts2020TableName+"\n")
  log.info("Using EMAIL Table: "+ emailTableName+"\n")


  config.getObject("akka.quartz.schedules").forEach((k, v) => log.info(s"$k = ${v.render()}"))

  actorSystem.actorOf(Props[PanelScheduler], "PanelScheduler")
  actorSystem.actorOf(Props[LarScheduler], "LarScheduler")
  actorSystem.actorOf(Props[TsScheduler], "TsScheduler")
  actorSystem.actorOf(Props[LarPublicScheduler], "LarPublicScheduler")
  actorSystem.actorOf(Props[TsPublicScheduler], "TsPublicScheduler")



}
// $COVERAGE-ON$