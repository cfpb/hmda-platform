package hmda.publication.lar

import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.kafka.scaladsl.Consumer.DrainingControl
import org.apache.pekko.kafka.{ConsumerSettings, Subscriptions}
import org.apache.pekko.pattern.ask
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.{Keep, Sink, Source}
import org.apache.pekko.util.Timeout
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

  val kafkaConfig = system.settings.config.getConfig("pekko.kafka.consumer")
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
    .mapMaterializedValue {
      case (control, future) => DrainingControl.apply(control, future)
    }
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
