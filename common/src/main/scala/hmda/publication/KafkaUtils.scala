package hmda.publication

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source}
import com.typesafe.config.ConfigFactory
import hmda.messages.institution.InstitutionEvents.InstitutionKafkaEvent
import hmda.serialization.kafka.InstitutionKafkaEventsSerializer
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.{ProducerRecord, Producer => KafkaProducer}
import org.apache.kafka.common.config.{SslConfigs,SaslConfigs}
//import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

object KafkaUtils {

  val config     = ConfigFactory.load()
  val kafkaHosts = config.getString("kafka.hosts")
  // val truststoreLocation = config.getString("kafka.ssl.truststore.location")
  // val truststorePassword = config.getString("kafka.ssl.truststore.password")
  // val endpointIdAlgo = config.getString("kafka.ssl.endpoint")
  val securityprotocol = config.getString("kafka.security.protocol")
  val saslmechanism = config.getString("kafka.sasl.mechanism")
  val sasljaasconfig= config.getString("kafka.sasl.jaas.config")
  val saslclientcallbackhandler= config.getString("kafka.sasl.client.callback.handler.class")

  def getStringKafkaProducer(system: ActorSystem[_]): KafkaProducer[String, String] = {

    val kafkaConfig = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(kafkaConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers(kafkaHosts)
        .withProperties(getKafkaConfig)

    producerSettings.createKafkaProducer()
  }

  def getInstitutionKafkaProducer(system: ActorSystem[_]): KafkaProducer[String, InstitutionKafkaEvent] = {
    val producerSettings =
      ProducerSettings(system.toClassic, new StringSerializer, new InstitutionKafkaEventsSerializer)
        .withBootstrapServers(kafkaHosts)
        .withProperties(getKafkaConfig)
    producerSettings.createKafkaProducer()
  }

  def getKafkaConfig: Map[String, String] = {

    if( securityprotocol=="SASL_SSL") {
    //if (!truststoreLocation.isEmpty && !truststorePassword.isEmpty) {
      Map(
        //CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> SecurityProtocol.SSL.name,
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> securityprotocol,
        CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL -> saslmechanism,

        //SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG -> truststoreLocation,
        //SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG -> truststorePassword,
        //SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG -> endpointIdAlgo,

        SaslConfigs.SASL_MECHANISM -> saslmechanism,
        SaslConfigs.SASL_JAAS_CONFIG -> sasljaasconfig,
        SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS -> saslclientcallbackhandler
      )
    } else {
      Map()
    }
  }

  def produceInstitutionRecord(
                                topic: String,
                                key: String,
                                value: InstitutionKafkaEvent,
                                kafkaProducer: KafkaProducer[String, InstitutionKafkaEvent]
                              )(implicit system: ActorSystem[_], materializer: Materializer): Future[Done] = {

    val producerSettings =
      ProducerSettings(system.toClassic, new StringSerializer, new InstitutionKafkaEventsSerializer)
        .withBootstrapServers(kafkaHosts)
        .withProperties(getKafkaConfig)
        .withProducer(kafkaProducer)

    Source
      .single(new ProducerRecord(topic, key, value))
      .toMat(Producer.plainSink(producerSettings))(Keep.right)
      .run()
  }

  def produceRecord(topic: String, key: String, value: String, producer: KafkaProducer[String, String])(
    implicit system: ActorSystem[_],
    materializer: Materializer
  ): Future[Done] = {

    val producerSettings =
      ProducerSettings(system.toClassic, new StringSerializer, new StringSerializer)
        .withBootstrapServers(kafkaHosts)
        .withProperties(getKafkaConfig)
        .withProducer(producer)

    Source
      .single(new ProducerRecord(topic, key, value))
      .toMat(Producer.plainSink(producerSettings))(Keep.right)
      .run()
  }

}
