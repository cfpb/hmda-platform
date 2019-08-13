package hmda.regulator

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import hmda.regulator.scheduler._
import org.slf4j.LoggerFactory
import hmda.model.census.Census
import akka.http.scaladsl.Http
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import hmda.publication.lar.publication._
import hmda.publication.lar.services.CensusRecordsRetriever
import akka.stream.ActorMaterializer
import hmda.publication.lar._
import hmda.publication.lar.services._
import akka.util.Timeout
import scala.concurrent.duration._

import hmda.publication.lar.services._

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

  val panelPublicTimer2018 = config.getString("akka.PanelPublicScheduler2018")
  val larPublicTimer2018 = config.getString("akka.LarPublicScheduler2018")
  val tsPublicTimer2018 = config.getString("akka.TsPublicScheduler2018")

  log.info("Panel Timer 2018: " + panelTimer2018)
  log.info("LAR Timer 2018: " + larTimer2018)
  log.info("TS Timer 2018: " + tsTimer2018)

  log.info("Panel Timer 2019: " + panelTimer2019)
  log.info("LAR Timer 2019: " + larTimer2019)
  log.info("TS Timer 2019: " + tsTimer2019)

  log.info("Panel Public Timer 2018: " + panelPublicTimer2018)
  log.info("LAR Public 2018: " + larPublicTimer2018)
  log.info("TS Public 2018: " + tsPublicTimer2018)

  val panelActorSystem =
    ActorSystem(
      "panelTask",
      ConfigFactory
        .parseString(panelTimer2018)
        .withValue(panelTimer2019(0),
                   ConfigValueFactory.fromAnyRef(panelTimer2019(1)))
        .withFallback(config)
    )
  panelActorSystem.actorOf(Props[PanelScheduler], "PanelScheduler")

  val larActorSystem =
    ActorSystem("larTask",
                ConfigFactory
                  .parseString(larTimer2018)
                  .withValue(larTimer2019(0),
                             ConfigValueFactory
                               .fromAnyRef(larTimer2019(1)))
                  .withFallback(config))
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(1.hour)
  val censusUrl = "http://census-api.default.svc.cluster.local:9093"
  val censusDownloader =
    CensusRecordsRetriever(Http(), censusUrl)
  val censusTractMap: Future[Map[String, Census]] =
    censusDownloader.downloadCensusMap(Tract)
  censusTractMap.onComplete {
    case Success(tractMap) =>
      larActorSystem.actorOf(Props(new LarScheduler(tractMap)), "LarScheduler")
    case Failure(exception) =>
      log.error(
        "Failed to download maps from Census API, cannot proceed, shutting down",
        exception)
      larActorSystem.terminate()
  }

  val tsActorSystem = ActorSystem("tsTask",
                                  ConfigFactory
                                    .parseString(tsTimer2018)
                                    .withValue(tsTimer2019(0),
                                               ConfigValueFactory
                                                 .fromAnyRef(tsTimer2019(1)))
                                    .withFallback(config))
  tsActorSystem.actorOf(Props[TsScheduler], "TsScheduler")

  //Public Data Actor Systems

  val panelPublicActorSystem =
    ActorSystem(
      "panelPublicTask",
      ConfigFactory.parseString(panelPublicTimer2018).withFallback(config))
  panelPublicActorSystem.actorOf(Props[PanelPublicScheduler],
                                 "PanelPublicScheduler")

  val larPublicActorSystem =
    ActorSystem(
      "larPublicTask",
      ConfigFactory.parseString(larPublicTimer2018).withFallback(config))
  larPublicActorSystem.actorOf(Props[LarPublicScheduler], "LarPublicScheduler")

  val tsPublicActorSystem =
    ActorSystem(
      "tsPublicTask",
      ConfigFactory.parseString(tsPublicTimer2018).withFallback(config))
  tsPublicActorSystem.actorOf(Props[TsPublicScheduler], "TsPublicScheduler")

}
