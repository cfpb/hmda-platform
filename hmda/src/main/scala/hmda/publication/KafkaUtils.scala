package hmda.publication

import java.time.Instant

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}

import scala.concurrent.Future

object KafkaUtils {

  val config = ConfigFactory.load()
  val kafkaHosts = config.getString("kafka.hosts")

  def getKafkaProducer(system: ActorSystem): KafkaProducer[String, String] = {

    val kafkaConfig = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(kafkaConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers(kafkaHosts)

    producerSettings.createKafkaProducer()
  }

  def produceRecord(topic: String, key: String, value: String)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer): Future[Done] = {

    val producerSettings =
      ProducerSettings(system, new StringSerializer, new StringSerializer)
        .withBootstrapServers(kafkaHosts)

    Source
      .single(new ProducerRecord(topic, key, value))
      .toMat(Producer.plainSink(producerSettings))(Keep.right)
      .run()

  }

}
