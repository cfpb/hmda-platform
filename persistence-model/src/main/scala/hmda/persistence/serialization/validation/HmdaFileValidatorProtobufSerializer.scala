package hmda.persistence.serialization.validation

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.persistence.model.serialization.HmdaFileValidatorEvents._
import hmda.persistence.serialization.validation.HmdaFileValidatorProtobufConverter._

class HmdaFileValidatorProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1006

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val EditsVerifiedManifest = classOf[EditsVerified].getName
  final val TsSyntacticalErrorManifest = classOf[TsSyntacticalError].getName
  final val TsValidityErrorManifest = classOf[TsValidityError].getName
  final val TsQualityErrorManifest = classOf[TsQualityError].getName
  final val LarSyntacticalErrorManifest = classOf[LarSyntacticalError].getName
  final val LarValidityErrorManifest = classOf[LarValidityError].getName
  final val LarQualityErrorManifest = classOf[LarQualityError].getName
  final val LarMacroErrorManifest = classOf[LarMacroError].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: EditsVerified => editsVerifiedToProtobuf(evt).toByteArray
    case evt: TsSyntacticalError => tsSyntacticalErrorToProtobuf(evt).toByteArray
    case evt: TsValidityError => tsValidityErrorToProtobuf(evt).toByteArray
    case evt: TsQualityError => tsQualityErrorToProtobuf(evt).toByteArray
    case evt: LarSyntacticalError => larSyntacticalErrorToProtobuf(evt).toByteArray
    case evt: LarValidityError => larValidityErrorToProtobuf(evt).toByteArray
    case evt: LarQualityError => larQualityErrorToProtobuf(evt).toByteArray
    case evt: LarMacroError => larMacroErrorToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize message $msg")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case EditsVerifiedManifest => editsVerifiedFromProtobuf(EditsVerifiedMessage.parseFrom(bytes))
    case TsSyntacticalErrorManifest => tsSyntacticalErrorFromProtobuf(TsSyntacticalErrorMessage.parseFrom(bytes))
    case TsValidityErrorManifest => tsValidityErrorFromProtobuf(TsValidityErrorMessage.parseFrom(bytes))
    case TsQualityErrorManifest => tsQualityErrorFromProtobuf(TsQualityErrorMessage.parseFrom(bytes))
    case LarSyntacticalErrorManifest => larSyntacticalErrorFromProtobuf(LarSyntacticalErrorMessage.parseFrom(bytes))
    case LarValidityErrorManifest => larValidityErrorFromProtobuf(LarValidityErrorMessage.parseFrom(bytes))
    case LarQualityErrorManifest => larQualityErrorFromProtobuf(LarQualityErrorMessage.parseFrom(bytes))
    case LarMacroErrorManifest => larMacroErrorFromProtobuf(LarMacroErrorMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize message $msg")
  }
}
