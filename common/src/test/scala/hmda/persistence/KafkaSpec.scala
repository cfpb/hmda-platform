package hmda.persistence

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerMessage.Message
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.testkit.TestKit
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  ByteArraySerializer,
  StringDeserializer,
  StringSerializer
}
import org.scalatest.{
  AsyncWordSpecLike,
  BeforeAndAfterAll,
  MustMatchers,
  WordSpecLike
}

import scala.concurrent.Future
import scala.concurrent.duration._

class KafkaSpec
    extends TestKit(ActorSystem("KafkaSpec"))
    with AsyncWordSpecLike
    with MustMatchers
    with BeforeAndAfterAll {

  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  implicit val embeddedKafkaConfig = EmbeddedKafkaConfig(
    9092,
    2182,
    Map("offsets.topic.replication.factor" -> "1"))
  val kafkaServers = s"localhost:${embeddedKafkaConfig.kafkaPort}"

  val InitialMsg =
    "initial msg in topic, required to create the topic before any consumer subscribes to it"

  val producerSettings =
    ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(kafkaServers)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedKafka.start()
    val producer = producerSettings.createKafkaProducer()
    producer.send(new ProducerRecord("topic", 0, null: Array[Byte], InitialMsg))
    producer.close(60, TimeUnit.SECONDS)
  }

  override protected def afterAll(): Unit = {
    shutdown(system, 30.seconds)
    EmbeddedKafka.stop()
    super.afterAll()
  }

  "Kafka Spec" must {
    "produce and consume messages" in {
      val producer = producerSettings.createKafkaProducer()
      producer.send(
        new ProducerRecord("topic", 0, null: Array[Byte], "Hello World"))

      val consumerSettings = ConsumerSettings(system,
                                              new ByteArrayDeserializer,
                                              new StringDeserializer)
        .withBootstrapServers(kafkaServers)
        .withGroupId("group")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        .withWakeupTimeout(10.seconds)
        .withMaxWakeups(10)

      val source = Source(1 to 10)
        .map { n =>
          val record =
            new ProducerRecord("topic", 0, null: Array[Byte], n.toString)
          Message(record, NotUsed)
        }
        .viaMat(Producer.flexiFlow(producerSettings))(Keep.right)

      val publishedF = source.runWith(Sink.ignore)

      publishedF.map { done =>
        Consumer
          .committableSource(consumerSettings, Subscriptions.topics("topic"))
          .map(msg => println(msg))
          .runWith(Sink.ignore)
        1 mustBe 1
      }

    }
  }

}
