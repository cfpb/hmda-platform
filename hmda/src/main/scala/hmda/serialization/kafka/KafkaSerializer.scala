package hmda.serialization.kafka

import java.util

import org.apache.kafka.common.serialization.Serializer

trait KafkaSerializer[A] extends Serializer[A] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}

}
