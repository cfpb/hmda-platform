package hmda.publication

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.typesafe.config.ConfigFactory
import hmda.messages.institution.InstitutionEvents.InstitutionKafkaEvent
import hmda.serialization.kafka.{
  InstitutionKafkaEventsDeserializer,
  InstitutionKafkaEventsSerializer
}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{
  StringDeserializer,
  StringSerializer
}

import scala.concurrent.{ExecutionContext, Future}

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

  def produceInstitutionRecord(topic: String,
                               key: String,
                               value: InstitutionKafkaEvent)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer): Future[Done] = {

    val producerSettings =
      ProducerSettings(system,
                       new StringSerializer,
                       new InstitutionKafkaEventsSerializer)
        .withBootstrapServers(kafkaHosts)

    Source
      .single(new ProducerRecord(topic, key, value))
      .toMat(Producer.plainSink(producerSettings))(Keep.right)
      .run()
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

  def consumeRecords(topic: String, f: Future[Done], parallelism: Int)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      ec: ExecutionContext) = {

    val config = system.settings.config.getConfig("akka.kafka.consumer")

    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(config, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(kafkaHosts)
        .withGroupId(UUID.randomUUID().toString)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(parallelism * 2) { msg =>
        f.map(_ => msg.committableOffset)
      }
      .mapAsync(parallelism)(offset => offset.commitScaladsl())
      .toMat(Sink.seq)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()

  }

  def consumeInstitutionRecords(topic: String,
                                f: Future[Done],
                                parallelism: Int)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      ec: ExecutionContext) = {

    val config = system.settings.config.getConfig("akka.kafka.consumer")

    val consumerSettings: ConsumerSettings[String, InstitutionKafkaEvent] =
      ConsumerSettings(config,
                       new StringDeserializer,
                       new InstitutionKafkaEventsDeserializer)
        .withBootstrapServers(kafkaHosts)
        .withGroupId("institution")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(parallelism * 2) { msg =>
        f.map(_ => msg.committableOffset)
      }
      .mapAsync(parallelism)(offset => offset.commitScaladsl())
      .toMat(Sink.seq)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()

  }

}
