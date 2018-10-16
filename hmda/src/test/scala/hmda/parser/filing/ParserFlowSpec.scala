package hmda.parser.filing

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer,
  StringSerializer
}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import org.apache.kafka.clients.producer.ProducerRecord
import ParserFlow._
import akka.util.ByteString
import hmda.model.filing.submission.SubmissionId

class ParserFlowSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()
  val kafkaServers = config.getString("kafka.servers")
  val rawTopic = "hmda-raw"

  val submissionId = SubmissionId("12345", "2018", 1)

  val ts = tsGen.sample.getOrElse(TransmittalSheet()).toCSV
  val lars = larNGen(10).sample
    .getOrElse(List(LoanApplicationRegister()))
    .map(lar =>
      lar.copy(larIdentifier = lar.larIdentifier.copy(LEI = submissionId.lei)))
    .map(_.toCSV)

  val messages = ts :: lars

  val producerSource = Source
    .fromIterator(() => messages.iterator)
    .map(value => new ProducerRecord[String, String](rawTopic, value))

  override def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedKafka.start()

    val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(producerConfig,
                       new StringSerializer,
                       new StringSerializer)
        .withBootstrapServers(kafkaServers)
    producerSource.runWith(Producer.plainSink(producerSettings))
  }

  override def afterAll(): Unit = {
    super.afterAll()
    EmbeddedKafka.stop()
    system.terminate()
  }

  "Parser" must {
    "consume from hmda raw kafka topic and parse them" in {
      val messagesWithKey = messages.map(m => (submissionId.toString, m))
      implicit val serializer = new StringSerializer
      EmbeddedKafka.publishToKafka(rawTopic, messagesWithKey)

      val consumerSettings =
        ConsumerSettings(system,
                         new ByteArrayDeserializer,
                         new StringDeserializer)
          .withBootstrapServers(kafkaServers)
          .withGroupId("raw-data")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

      Consumer
        .committableSource(consumerSettings, Subscriptions.topics(rawTopic))
        .drop(1)
        .map(e => e.record.value())
        .map(e => ByteString(e))
        .via(parseLarFlow)
        .runWith(Sink.seq)
        .map(e => println(e))
    }
  }

}
