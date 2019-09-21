package hmda.publication.lar

import akka.Done
import scala.util.{Failure, Success}
import akka.actor.{ActorRef, ActorSystem, Scheduler}
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.util.Timeout
import hmda.messages.pubsub.{HmdaGroups, HmdaTopics}
import hmda.publication.KafkaUtils.{config, kafkaHosts}
import hmda.publication.lar.sender.EmailSender
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

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
  val parallelism = config.getInt("hmda.lar.email.parallelism")

  def submitForEmail(emailSender: ActorRef[])(untypedSubmissionId: String, unTypedEmail: String)(implicit scheduler: Scheduler,
                                                timeout: Timeout): Future[Done] = {

  }

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")

  val emailSender = system.spawn(EmailSender.behavior(), EmailSender.name)

  val processKafkaRecord: String => Future[Done] = submitForEmail(emailSender)

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConfig,
      new StringDeserializer,
      new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId(HmdaGroups.emailGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  val (kafkaControl: Consumer.Control, streamCompleted: Future[Done]) =
    Consumer.committableSource(consumerSettings, Subscriptions.topics(HmdaTopics.emailTopic, HmdaGroups.emailGroup))
      .mapAsync(parallelism) { msg =>
        //key will be like B90SDFS-2018-33
        //value will be like someone@gmail.com
        log.info(s"Received a message - key: ${
          msg.record
            .key()
        }, value: ${msg.record.value()}")

        akka.pattern.retry(() =>
                processKafkaRecord(msg.record.value().trim)
          .map(_ => msg.committableOffset), 1, 90.seconds)

      }
    .mapAsync(parallelism * 2)((offset: ConsumerMessage.CommittableOffset) =>
      offset.commitScaladsl())
      .toMat(Sink.ignore)(Keep.both)
      .run()

  streamCompleted.onComplete {
    case Failure(exception) =>
      log.error("Kafka consumer infinite stream failed", exception)
      kafkaControl
        .shutdown()
        .flatMap(_ => system.terminate())
        .foreach(_ => sys.exit(1)) // The Cassandra connection stays up and prevents the app from shutting down even when the Kafka stream is not active, so we explicitly shut the application down

    case Success(res) =>
      log.error(
        "Kafka consumer infinite stream completed, an infinite stream should not complete")
      system
        .terminate()
        .foreach(_ => sys.exit(2))
  }

}
