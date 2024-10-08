package hmda.publication.lar

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.pattern.ask
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.census.records.CensusRecords
import hmda.messages.HmdaMessageFilter
import hmda.messages.pubsub.{HmdaGroups, HmdaTopics}
import hmda.model.census.Census
import hmda.model.filing.submission.SubmissionId
import hmda.publication.KafkaUtils._
import hmda.publication.lar.publication.{IrsPublisher, PublishIrs}
import hmda.util.BankFilterUtils._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

object IrsPublisherApp extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      |
      |,--.,------.  ,---.      ,------.         ,--.   ,--.,--.       ,--.
      ||  ||  .--. ''   .-'     |  .--. ',--.,--.|  |-. |  |`--' ,---. |  ,---.  ,---. ,--.--.
      ||  ||  '--'.'`.  `-.     |  '--' ||  ||  || .-. '|  |,--.(  .-' |  .-.  || .-. :|  .--'
      ||  ||  |\  \ .-'    |    |  | --' '  ''  '| `-' ||  ||  |.-'  `)|  | |  |\   --.|  |
      |`--'`--' '--'`-----'     `--'      `----'  `---' `--'`--'`----' `--' `--' `----'`--'
      |
    """.stripMargin
  )

  implicit val system       = ActorSystem()
  implicit val materializer = Materializer(system)
  implicit val ec           = system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  val kafkaConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val config      = ConfigFactory.load()
  val parallelism = config.getInt("hmda.lar.irs.parallelism")

  val irsPublisher =
    system.spawn(IrsPublisher.behavior(), IrsPublisher.name)

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId(HmdaGroups.irsGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperties(getKafkaConfig)

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics(HmdaTopics.signTopic, HmdaTopics.irsTopic))
    .mapAsync(parallelism)(HmdaMessageFilter.processOnlyValidKeys(msg => processData(msg.record.value().stripLineEnd.trim()).map(_ => msg.committableOffset)))
    .mapAsync(parallelism * 2)(offset => offset.commitScaladsl())
    .toMat(Sink.seq)(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()

  def processData(msg: String): Future[Done] =
    Source
      .single(msg)
      .filter { msg =>
        val submissionId = SubmissionId(msg)
        filterBankWithLogging(submissionId.lei) && filterQuarterlyFilingAlt(submissionId)
      }
      .map { msg =>
        val submissionId = SubmissionId(msg)
        irsPublisher.toClassic ? PublishIrs(submissionId)
      }
      .toMat(Sink.ignore)(Keep.right)
      .run()

}
