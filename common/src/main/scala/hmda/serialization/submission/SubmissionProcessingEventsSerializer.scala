package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.model.filing.submission.VerificationStatus
import hmda.model.processing.state.{ HmdaParserErrorState, HmdaValidationErrorState }
import hmda.persistence.serialization.submission.processing.events.{
  HmdaParserErrorStateMessage,
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage,
  _
}
import hmda.serialization.submission.SubmissionProcessingEventsProtobufConverter._
import hmda.serialization.validation.ValidationProtobufConverter._
// $COVERAGE-OFF$
class SubmissionProcessingEventsSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 108

  final val ParsedErrorManifest          = classOf[HmdaRowParsedError].getName
  final val ParsedErrorCountManifest     = classOf[HmdaRowParsedCount].getName
  final val HmdaParserErrorStateManifest = classOf[HmdaParserErrorState].getName
  final val HmdaValidationErrorStateManifest =
    classOf[HmdaValidationErrorState].getName
  final val HmdaRowValidatedErrorManifest =
    classOf[HmdaRowValidatedError].getName
  final val HmdaMacroValidatedErrorManifest =
    classOf[HmdaMacroValidatedError].getName
  final val QualityVerifiedManifest      = classOf[QualityVerified].getName
  final val MacroVerifiedManifest        = classOf[MacroVerified].getName
  final val NotReadyToBeVerifiedManifest = classOf[NotReadyToBeVerified].getName
  final val SyntacticalValidityCompletedManifest =
    classOf[SyntacticalValidityCompleted].getName
  final val QualityCompletedManifest = classOf[QualityCompleted].getName
  final val MacroCompletedManifest   = classOf[MacroCompleted].getName
  final val SubmissionSignedManifest = classOf[SubmissionSigned].getName
  final val SubmissionNotReadyToBeSignedManifest =
    classOf[SubmissionNotReadyToBeSigned].getName
  final val VerificationStatusManifest =
    classOf[VerificationStatus].getName

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
    case evt: HmdaMacroValidatedError =>
      hmdaMacroValidatedErrorToProtobuf(evt).toByteArray
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
    case evt: MacroCompleted =>
      macroCompletedToProtobuf(evt).toByteArray
    case evt: SubmissionSigned =>
      submissionSignedToProtobuf(evt).toByteArray
    case evt: SubmissionNotReadyToBeSigned =>
      submissionNotReadyToBeSignedToProtobuf(evt).toByteArray
    case evt: VerificationStatus =>
      verificationStatusToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case ParsedErrorManifest =>
        hmdaRowParsedErrorFromProtobuf(HmdaRowParsedErrorMessage.parseFrom(bytes))
      case ParsedErrorCountManifest =>
        hmdaRowParsedCountFromProtobuf(HmdaRowParsedCountMessage.parseFrom(bytes))
      case HmdaParserErrorStateManifest =>
        hmdaParserErrorStateFromProtobuf(HmdaParserErrorStateMessage.parseFrom(bytes))
      case HmdaValidationErrorStateManifest =>
        hmdaValidationErrorStateFromProtobuf(HmdaValidationErrorStateMessage.parseFrom(bytes))
      case HmdaRowValidatedErrorManifest =>
        hmdaRowValidatedErrorFromProtobuf(HmdaRowValidatedErrorMessage.parseFrom(bytes))
      case HmdaMacroValidatedErrorManifest =>
        hmdaMacroValidatedErrorFromProtobuf(HmdaMacroValidatedErrorMessage.parseFrom(bytes))
      case QualityVerifiedManifest =>
        qualityVerifiedFromProtobuf(QualityVerifiedMessage.parseFrom(bytes))
      case MacroVerifiedManifest =>
        macroVerifiedFromProtobuf(MacroVerifiedMessage.parseFrom(bytes))
      case NotReadyToBeVerifiedManifest =>
        notReadyToBeVerifiedFromProtobuf(NotReadyToBeVerifiedMessage.parseFrom(bytes))
      case SyntacticalValidityCompletedManifest =>
        syntacticalValidityCompletedFromProtobuf(SyntacticalValidityCompletedMessage.parseFrom(bytes))
      case QualityCompletedManifest =>
        qualityCompletedFromProtobuf(QualityCompletedMessage.parseFrom(bytes))
      case MacroCompletedManifest =>
        macroCompletedFromProtobuf(MacroCompletedMessage.parseFrom(bytes))
      case SubmissionSignedManifest =>
        submissionSignedFromProtobuf(SubmissionSignedMessage.parseFrom(bytes))
      case SubmissionNotReadyToBeSignedManifest =>
        submissionNotReadyToBeSignedFromProtobuf(SubmissionNotReadyToBeSignedMessage.parseFrom(bytes))
      case VerificationStatusManifest =>
        verificationStatusFromProtobuf(VerificationStatusMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
// $COVERAGE-ON$