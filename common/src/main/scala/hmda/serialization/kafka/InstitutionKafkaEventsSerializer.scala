package hmda.serialization.kafka

import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.serialization.institution.InstitutionEventsSerializer
import org.apache.kafka.common.serialization.Serializer

class InstitutionKafkaEventsSerializer extends Serializer[InstitutionEvent] {
  override def configure(configs: java.util.Map[String,_],isKey: Boolean):Unit = {

  }

  override def serialize(topic:String, institutionEvent: InstitutionEvent): Array[Byte] = {
    val serializer = new InstitutionEventsSerializer
    serializer.toBinary(institutionEvent)
  }

  override def close():Unit = {

  }
}
