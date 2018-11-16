package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.serialization.validation.ValidationProtobufConverter._
import hmda.model.processing.state.HmdaValidationErrorState
import SubmissionProcessingEventsProtobufConverter._
import hmda.model.processing.state.HmdaParserErrorState
import hmda.persistence.serialization.submission.processing.events.{
  HmdaParserErrorStateMessage,
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage
}
import hmda.persistence.serialization.submission.processing.events._

class SubmissionProcessingEventsSerializer
    extends SerializerWithStringManifest {
  override def identifier: Int = 108

  final val ParsedErrorManifest = classOf[HmdaRowParsedError].getName
  final val ParsedErrorCountManifest = classOf[HmdaRowParsedCount].getName
  final val HmdaParserErrorStateManifest = classOf[HmdaParserErrorState].getName
  final val HmdaValidationErrorStateManifest =
    classOf[HmdaValidationErrorState].getName
  final val HmdaRowValidatedErrorManifest =
    classOf[HmdaRowValidatedError].getName
  final val QualityVerifiedManifest = classOf[QualityVerified].getName
  final val MacroVerifiedManifest = classOf[MacroVerified].getName
  final val NotReadyToBeVerifiedManifest = classOf[NotReadyToBeVerified].getName
  final val SyntacticalValidityCompletedManifest =
    classOf[SyntacticalValidityCompleted].getName
  final val QualityCompletedManifest = classOf[QualityCompleted].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: HmdaRowParsedError =>
      hmdaRowParsedErrorToProtobuf(evt).toByteArray
    case evt: HmdaRowParsedCount =>
      hmdaRowParsedCountToProtobuf(evt).toByteArray
    case evt: HmdaParserErrorState =>
      hmdaParserErrorStateToProtobuf(evt).toByteArray
    case evt: HmdaValidationErrorState =>
      hmdaValidationErrorStateToProtobuf(evt).toByteArray
    case evt: HmdaRowValidatedError =>
      hmdaRowValidatedErrorToProtobuf(evt).toByteArray
    case evt: QualityVerified =>
      qualityVerifiedToProtobuf(evt).toByteArray
    case evt: MacroVerified =>
      macroVerifiedToProtobuf(evt).toByteArray
    case evt: NotReadyToBeVerified =>
      notReadyToBeVerifiedToProtobuf(evt).toByteArray
    case evt: SyntacticalValidityCompleted =>
      syntacticalValidityCompletedToProtobuf(evt).toByteArray
    case evt: QualityCompleted =>
      qualityCompletedToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(
        s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case ParsedErrorManifest =>
        hmdaRowParsedErrorFromProtobuf(
          HmdaRowParsedErrorMessage.parseFrom(bytes))
      case ParsedErrorCountManifest =>
        hmdaRowParsedCountFromProtobuf(
          HmdaRowParsedCountMessage.parseFrom(bytes))
      case HmdaParserErrorStateManifest =>
        hmdaParserErrorStateFromProtobuf(
          HmdaParserErrorStateMessage.parseFrom(bytes))
      case HmdaValidationErrorStateManifest =>
        hmdaValidationErrorStateFromProtobuf(
          HmdaValidationErrorStateMessage.parseFrom(bytes))
      case HmdaRowValidatedErrorManifest =>
        hmdaRowValidatedErrorFromProtobuf(
          HmdaRowValidatedErrorMessage.parseFrom(bytes))
      case QualityVerifiedManifest =>
        qualityVerifiedFromProtobuf(QualityVerifiedMessage.parseFrom(bytes))
      case MacroVerifiedManifest =>
        macroVerifiedFromProtobuf(MacroVerifiedMessage.parseFrom(bytes))
      case NotReadyToBeVerifiedManifest =>
        notReadyToBeVerifiedFromProtobuf(
          NotReadyToBeVerifiedMessage.parseFrom(bytes))
      case SyntacticalValidityCompletedManifest =>
        syntacticalValidityCompletedFromProtobuf(
          SyntacticalValidityCompletedMessage.parseFrom(bytes))
      case QualityCompletedManifest =>
        qualityCompletedFromProtobuf(QualityCompletedMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
