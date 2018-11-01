package hmda.serialization.kafka.ts

import hmda.model.filing.ts.TransmittalSheet
import hmda.serialization.kafka.KafkaSerializer
import hmda.serialization.filing.ts.TransmittalSheetProtobufConverter._

class TsKafkaSerializer extends KafkaSerializer[TransmittalSheet] {
  override def serialize(topic: String, data: TransmittalSheet): Array[Byte] = {
    transmittalSheetToProtobuf(data).toByteArray
  }
}
