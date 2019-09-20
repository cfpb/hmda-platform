package hmda.publication.lar

import akka.actor.{ActorSystem, Scheduler}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.publication.KafkaUtils.config
import hmda.publication.lar.sender.EmailSender
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

class EmailReceiptApp extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      | _   _ ___  ________  ___    _____                _ _
      || | | ||  \/  |  _  \/ _ \  |  ___|              (_) |
      || |_| || .  . | | | / /_\ \ | |__ _ __ ___   __ _ _| |
      ||  _  || |\/| | | | |  _  | |  __| '_ ` _ \ / _` | | |
      || | | || |  | | |/ /| | | | | |__| | | | | | (_| | | |
      |\_| |_/\_|  |_/___/ \_| |_/ \____/_| |_| |_|\__,_|_|_|
      |
      |""".stripMargin)

  implicit val system: ActorSystem = ActorSystem()
  implicit val materealizer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = Timeout(1.hour)

  val kafkaConfig = config.getConfig("akka.kafka.consumer")
  val parallelism = config.getInt("hmda.lar.modified.parallelism")

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")

  val emailSender = system.spawn(EmailSender.behavior(), EmailSender.name)

}
