package hmda.publication.lar

import akka.Done
import akka.actor.{ ActorSystem, Scheduler }
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ ConsumerMessage, ConsumerSettings, Subscriptions }
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.Timeout
import hmda.messages.pubsub.HmdaGroups
import hmda.model.census.Census
import hmda.model.filing.submission.SubmissionId
import hmda.publication.KafkaUtils._
import hmda.publication.lar.publication._
import hmda.messages.pubsub.HmdaTopics
import hmda.query.repository.ModifiedLarRepository
import hmda.util.BankFilterUtils._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import hmda.census.records._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success }

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

  implicit val system: ActorSystem             = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor    = system.dispatcher
  implicit val scheduler: Scheduler            = system.scheduler
  implicit val timeout: Timeout                = Timeout(1.hour)

  val kafkaConfig      = config.getConfig("akka.kafka.consumer")
  val bankFilterConfig = config.getConfig("filter")
  val bankFilterList =
    bankFilterConfig.getString("bank-filter-list").toUpperCase.split(",")
  val parallelism = config.getInt("hmda.lar.modified.parallelism")

  val censusTractMap2018: Map[String, Census] =
    CensusRecords.indexedTract2018

  val censusTractMap2019: Map[String, Census] =
    CensusRecords.indexedTract2019

  def submitForPersistence(
    modifiedLarPublisher: ActorRef[PersistToS3AndPostgres]
  )(untypedSubmissionId: String)(implicit scheduler: Scheduler, timeout: Timeout): Future[Done] = {
    val submissionId = SubmissionId(untypedSubmissionId)
    if (!filterBankWithLogging(submissionId.lei, bankFilterList) || filterQuarterlyFiling(submissionId))
      Future.successful(Done.done())
    else {
      val futRes: Future[PersistModifiedLarResult] =
        modifiedLarPublisher ? ((ref: ActorRef[PersistModifiedLarResult]) => PersistToS3AndPostgres(submissionId, ref))
      // bubble up failure if there is any (this is done to prevent a commit to Kafka from happening
      // when used in Kafka consumer Akka Stream
      futRes.map(result => result.status).flatMap {
        case UploadSucceeded         => Future(Done.done())
        case UploadFailed(exception) => Future.failed(exception)
      }
    }
  }

  // database configuration is located in `common` project
  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  val repo           = new ModifiedLarRepository(databaseConfig)
  val modifiedLarPublisher =
    system.spawn(ModifiedLarPublisher.behavior(censusTractMap2018, censusTractMap2019, repo), ModifiedLarPublisher.name)
  val processKafkaRecord: String => Future[Done] =
    submitForPersistence(modifiedLarPublisher)

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId(HmdaGroups.modifiedLarGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  val (kafkaControl: Consumer.Control, streamCompleted: Future[Done]) =
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(HmdaTopics.signTopic, HmdaTopics.modifiedLarTopic))
      .mapAsync(parallelism) { msg =>
        log.info(s"Received a message - key: ${msg.record
          .key()}, value: ${msg.record.value()}")

        // TODO: Make configurable
        // retry the Future 10 times using a spacing of 30 seconds if there is a failure before giving up
        akka.pattern.retry(
          () =>
            processKafkaRecord(msg.record.value().trim)
              .map(_ => msg.committableOffset),
          1,
          90.seconds
        )
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
      log.error("Kafka consumer infinite stream completed, an infinite stream should not complete")
      system
        .terminate()
        .foreach(_ => sys.exit(2))
  }

}
