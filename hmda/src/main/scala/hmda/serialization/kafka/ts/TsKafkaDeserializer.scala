package hmda.serialization.kafka.ts

import hmda.model.filing.ts.TransmittalSheet
import hmda.persistence.serialization.transmittalsheet.TransmittalSheetMessage
import hmda.serialization.kafka.KafkaDeserializer
import hmda.serialization.filing.ts.TransmittalSheetProtobufConverter._

class TsKafkaDeserializer extends KafkaDeserializer[TransmittalSheet] {
  override def deserialize(topic: String,
                           data: Array[Byte]): TransmittalSheet = {
    transmittalSheetFromProtobuf(TransmittalSheetMessage.parseFrom(data))
  }
}
