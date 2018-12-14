package hmda.publication.lar

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.slf4j.LoggerFactory
import hmda.publication.KafkaUtils._
import hmda.messages.pubsub.HmdaTopics._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import akka.actor.typed.scaladsl.adapter._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.filing.submission.SubmissionId
import hmda.publication.lar.publication.{ModifiedLarPublisher, UploadToS3}

import scala.concurrent.Future
import scala.concurrent.duration._

object ModifiedLarApp extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      |___  ___          _ _  __ _          _   _       ___  ______
      ||  \/  |         | (_)/ _(_)        | | | |     / _ \ | ___ \
      || .  . | jmo   __| |_| |_ _  ___  __| | | |    / /_\ \| |_/ /
      || |\/| |/ _ \ / _` | |  _| |/ _ \/ _` | | |    |  _  ||    /
      || |  | | (_) | (_| | | | | |  __/ (_| | | |____| | | || |\ \
      |\_|  |_/\___/ \__,_|_|_| |_|\___|\__,_| \_____/\_| |_/\_| \_|
      |
    """.stripMargin
  )

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  val kafkaConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val config = ConfigFactory.load()

  val parallelism = config.getInt("hmda.lar.modified.parallelism")

  val modifiedLarPublisher =
    system.spawn(ModifiedLarPublisher.behavior, ModifiedLarPublisher.name)

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConfig,
                     new StringDeserializer,
                     new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId("modified-lar")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  Consumer
    .committableSource(consumerSettings,
                       Subscriptions.topics(signTopic, modifiedLarTopic))
    .mapAsync(parallelism) { msg =>
      processData(msg.record.value()).map(_ => msg.committableOffset)
    }
    .mapAsync(parallelism * 2)(offset => offset.commitScaladsl())
    .toMat(Sink.seq)(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()

  def processData(msg: String): Future[Done] = {
    Source
      .single(msg)
      .map { msg =>
        val submissionId = SubmissionId(msg)
        modifiedLarPublisher.toUntyped ? UploadToS3(submissionId)
      }
      .toMat(Sink.ignore)(Keep.right)
      .run()

  }

}
