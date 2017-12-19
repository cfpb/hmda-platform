package hmda.persistence.serialization.validation

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.validation.SubmissionLarStatsEvents.{ IrsStatsUpdated, MacroStatsUpdated, SubmittedLarsUpdated, ValidatedLarsUpdated }
import hmda.persistence.model.serialization.SubmissionLarStatsEvents.{ IrsStatsUpdatedMessage, MacroStatsUpdatedMessage, SubmittedLarsUpdatedMessage, ValidatedLarsUpdatedMessage }
import hmda.persistence.serialization.validation.SubmissionLarStatsProtobufConverter._

class SubmissionLarStatsProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1007

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val SubmittedLarsUpdatedManifest = classOf[SubmittedLarsUpdated].getName
  final val ValidatedLarsUpdatedManifest = classOf[ValidatedLarsUpdated].getName
  final val MacroStatsUpdatedManifest = classOf[MacroStatsUpdated].getName
  final val IrsStatsUpdatedManifest = classOf[IrsStatsUpdated].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: SubmittedLarsUpdated => submittedLarsUpdatedToProtobuf(evt).toByteArray
    case evt: ValidatedLarsUpdated => validatedLarsUpdatedToProtobuf(evt).toByteArray
    case evt: MacroStatsUpdated => macroStatsUpdatedToProtobuf(evt).toByteArray
    case evt: IrsStatsUpdated => irsStatsUpdatedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case SubmittedLarsUpdatedManifest =>
      submittedLarsUpdatedFromProtobuf(SubmittedLarsUpdatedMessage.parseFrom(bytes))
    case ValidatedLarsUpdatedManifest =>
      validatedLarsUpdatedFromProtobuf(ValidatedLarsUpdatedMessage.parseFrom(bytes))
    case MacroStatsUpdatedManifest =>
      macroStatsUpdatedFromProtobuf(MacroStatsUpdatedMessage.parseFrom(bytes))
    case IrsStatsUpdatedManifest =>
      irsStatsUpdatedFromProtobuf(IrsStatsUpdatedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
