package hmda.serialization.kafka

import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.serialization.institution.InstitutionEventsSerializer
import org.apache.kafka.common.serialization.Deserializer

class InstitutionKafkaEventsDeserializer extends Deserializer[InstitutionEvent] {
  override def configure(configs: java.util.Map[String,_],isKey: Boolean):Unit = {

  }

  override def deserialize(topic:String, bytes: Array[Byte]): InstitutionEvent = {
    val serializer = new InstitutionEventsSerializer
    serializer.InstitutionCreatedManifest
    serializer.InstitutionModifiedManifest
    serializer.InstitutionDeletedManifest
    serializer.fromBinary(bytes)
  }

  override def close():Unit = {

  }
}
