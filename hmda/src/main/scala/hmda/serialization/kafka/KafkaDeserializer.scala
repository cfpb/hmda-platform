package hmda.serialization.kafka

import java.util

import org.apache.kafka.common.serialization.Deserializer

trait KafkaDeserializer[A] extends Deserializer[A] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}

}
