package hmda.persistence.serialization.filing

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.serialization.HmdaFilingEvents.LarValidatedMessage
import hmda.persistence.serialization.filing.HmdaFilingProtobufConverter._

class HmdaFilingProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1004

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val LarValidatedManifest = classOf[LarValidated].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: LarValidated => larValidatedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case LarValidatedManifest =>
      larValidatedFromProtobuf(LarValidatedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
