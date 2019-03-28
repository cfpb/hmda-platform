package hmda.publication.lar

import akka.Done
import akka.actor.{ActorSystem, Scheduler}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.messages.pubsub.HmdaTopics._
import hmda.model.census.Census
import hmda.model.filing.submission.SubmissionId
import hmda.publication.KafkaUtils._
import hmda.publication.lar.publication._
import hmda.publication.lar.services._
import hmda.query.repository.ModifiedLarRepository
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ModifiedLarApp extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      |___  ___          _ _  __ _          _   _       ___  ______
      ||  \/  |         | (_)/ _(_)        | | | |     / _ \ | ___ \
      || .  . | ___   __| |_| |_ _  ___  __| | | |    / /_\ \| |_/ /
      || |\/| |/ _ \ / _` | |  _| |/ _ \/ _` | | |    |  _  ||    /
      || |  | | (_) | (_| | | | | |  __/ (_| | | |____| | | || |\ \
      |\_|  |_/\___/ \__,_|_|_| |_|\___|\__,_| \_____/\_| |_/\_| \_|
      |
    """.stripMargin
  )

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = Timeout(1.hour)
  val config = ConfigFactory.load()
  val censusUrl = "http://" + config.getString("hmda.census.http.host") + ":" + config
    .getString("hmda.census.http.port")
  val censusDownloader =
    CensusRecordsRetriever(Http(), censusUrl)

  val kafkaConfig = config.getConfig("akka.kafka.consumer")
  val bankFilterConfig = config.getConfig("filter")
  val bankFilterList =
    bankFilterConfig.getString("bank-filter-list").toUpperCase.split(",")
  val parallelism = config.getInt("hmda.lar.modified.parallelism")

  // NOTE: Modified LAR has a hard dependency on Census API and cannot function without it
  val censusTractMap: Future[Map[String, Census]] =
    censusDownloader.downloadCensusMap(Tract)

  def submitForPersistence(
      modifiedLarPublisher: ActorRef[PersistToS3AndPostgres])(
      untypedSubmissionId: String)(implicit scheduler: Scheduler,
                                   timeout: Timeout): Future[Done] = {
    val submissionId = SubmissionId(untypedSubmissionId)
    if (bankFilterList.exists(
          bankLEI => bankLEI.equalsIgnoreCase(submissionId.lei)))
      Future.successful(Done.done())
    else {
      val futRes: Future[PersistModifiedLarResult] =
        modifiedLarPublisher ? ((ref: ActorRef[PersistModifiedLarResult]) =>
          PersistToS3AndPostgres(submissionId, ref))
      // bubble up failure if there is any (this is done to prevent a commit to Kafka from happening
      // when used in Kafka consumer Akka Stream
      futRes.map(result => result.status).flatMap {
        case UploadSucceeded         => Future(Done.done())
        case UploadFailed(exception) => Future.failed(exception)
      }
    }
  }

  censusTractMap.onComplete {
    case Success(tractMap) =>
      // database configuration is located in `common` project
      val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
      val repo = new ModifiedLarRepository("modifiedlar2018", databaseConfig)
      val modifiedLarPublisher =
        system.spawn(ModifiedLarPublisher.behavior(tractMap, repo),
                     ModifiedLarPublisher.name)
      val processKafkaRecord: String => Future[Done] =
        submitForPersistence(modifiedLarPublisher)

      val consumerSettings: ConsumerSettings[String, String] =
        ConsumerSettings(kafkaConfig,
                         new StringDeserializer,
                         new StringDeserializer)
          .withBootstrapServers(kafkaHosts)
          .withGroupId("modified-lar")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

      val (kafkaControl: Consumer.Control, streamCompleted: Future[Done]) =
        Consumer
          .committableSource(consumerSettings,
                             Subscriptions.topics(signTopic, modifiedLarTopic))
          .mapAsync(parallelism) { msg =>
            log.info(s"Received a message - key: ${msg.record
              .key()}, value: ${msg.record.value()}")

            // TODO: Make configurable
            // retry the Future 10 times using a spacing of 30 seconds if there is a failure before giving up
            akka.pattern.retry(() =>
                                 processKafkaRecord(msg.record.value()).map(_ =>
                                   msg.committableOffset),
                               10,
                               30.seconds)
          }
          .mapAsync(parallelism * 2)((offset: ConsumerMessage.CommittableOffset) => offset.commitScaladsl())
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

    case Failure(exception) =>
      log.error(
        "Failed to download maps from Census API, cannot proceed, shutting down",
        exception)
      system.terminate()
  }
}
