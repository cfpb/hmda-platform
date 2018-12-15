package hmda.analytics

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.messages.pubsub.HmdaTopics.{analyticsTopic, signTopic}
import hmda.publication.KafkaUtils.kafkaHosts
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

object HmdaAnalyticsApp extends App {

  val log = LoggerFactory.getLogger("hmda")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  val kafkaConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val config = ConfigFactory.load()

  val parallelism = config.getInt("hmda.analytics.parallelism")

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConfig,
                     new StringDeserializer,
                     new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId("hmda-analytics")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  Consumer
    .committableSource(consumerSettings,
                       Subscriptions.topics(signTopic, analyticsTopic))
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
//      .map { msg =>
//        val submissionId = SubmissionId(msg)
//        modifiedLarPublisher.toUntyped ? UploadToS3(submissionId)
//      }
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

}
