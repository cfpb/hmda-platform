package hmda.serialization.kafka

import hmda.messages.institution.InstitutionEvents.InstitutionKafkaEvent
import hmda.serialization.institution.InstitutionEventsProtobufConverter
import org.apache.kafka.common.serialization.Serializer

class InstitutionKafkaEventsSerializer extends Serializer[InstitutionKafkaEvent] {
  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, institutionKafkaEvent: InstitutionKafkaEvent): Array[Byte] =
    InstitutionEventsProtobufConverter
      .institutionKafkaEventToProtobuf(institutionKafkaEvent)
      .toByteArray

  override def close(): Unit = {}
}
