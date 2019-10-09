package hmda.serialization.kafka

import hmda.messages.institution.InstitutionEvents.InstitutionKafkaEvent
import hmda.serialization.institution.InstitutionEventsProtobufConverter
import org.apache.kafka.common.serialization.Deserializer

class InstitutionKafkaEventsDeserializer extends Deserializer[InstitutionKafkaEvent] {
  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, bytes: Array[Byte]): InstitutionKafkaEvent =
    InstitutionEventsProtobufConverter.institutionKafkaEventFromProtobuf(bytes)

  override def close(): Unit = {}
}
