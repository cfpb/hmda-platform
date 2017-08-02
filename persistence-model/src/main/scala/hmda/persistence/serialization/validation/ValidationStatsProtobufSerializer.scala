package hmda.persistence.serialization.validation

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.validation.ValidationStatsEvents._
import hmda.persistence.model.serialization.ValidationStatsEvents._
import hmda.persistence.serialization.validation.ValidationStatsProtobufConverter._

class ValidationStatsProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1008

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val SubmissionSubmittedTotalsAddedManifest = classOf[SubmissionSubmittedTotalsAdded].getName
  final val SubmissionTaxIdAddedManifest = classOf[SubmissionTaxIdAdded].getName
  final val SubmissionMacroStatsAddedManifest = classOf[SubmissionMacroStatsAdded].getName
  final val IrsStatsAddedManifest = classOf[IrsStatsAdded].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: SubmissionSubmittedTotalsAdded => submissionSubmittedTotalsAddedToProtobuf(evt).toByteArray
    case evt: SubmissionTaxIdAdded => submissionTaxIdAddedToProtobuf(evt).toByteArray
    case evt: SubmissionMacroStatsAdded => submissionMacroStatsAddedToProtobuf(evt).toByteArray
    case evt: IrsStatsAdded => irsStatsAddedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize message $msg")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case SubmissionSubmittedTotalsAddedManifest =>
      submissionSubmittedTotalsAddedFromProtobuf(SubmissionSubmittedTotalsAddedMessage.parseFrom(bytes))
    case SubmissionTaxIdAddedManifest =>
      submissionTaxIdAddedFromProtobuf(SubmissionTaxIdAddedMessage.parseFrom(bytes))
    case SubmissionMacroStatsAddedManifest =>
      submissionMacroStatsAddedFromProtobuf(SubmissionMacroStatsAddedMessage.parseFrom(bytes))
    case IrsStatsAddedManifest =>
      irsStatsAddedFromProtobuf(IrsStatsAddedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize message $msg")
  }
}
