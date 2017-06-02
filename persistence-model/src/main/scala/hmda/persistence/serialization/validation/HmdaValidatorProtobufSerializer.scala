package hmda.persistence.serialization.validation

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ LarValidated, TsValidated }
import hmda.persistence.model.serialization.CommonHmdaValidator.{ LarValidatedMessage, TsValidatedMessage }
import hmda.persistence.serialization.validation.HmdaValidatorProtobufConverter._

class HmdaValidatorProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1004

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val LarValidatedManifest = classOf[LarValidated].getName
  final val TsValidatedManifest = classOf[TsValidated].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: LarValidated => larValidatedToProtobuf(evt).toByteArray
    case evt: TsValidated => tsValidatedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case LarValidatedManifest =>
      larValidatedFromProtobuf(LarValidatedMessage.parseFrom(bytes))
    case TsValidatedManifest =>
      tsValidatedFromProtobuf(TsValidatedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
