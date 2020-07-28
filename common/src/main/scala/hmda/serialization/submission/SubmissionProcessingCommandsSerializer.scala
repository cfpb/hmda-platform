package hmda.serialization.submission

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter._
import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionProcessingCommands.{ CompleteSyntacticalValidity, GetHmdaValidationErrorState, _ }
import hmda.persistence.serialization.submission.processing.commands._
import hmda.serialization.submission.SubmissionProcessingCommandsProtobufConverter._
// $COVERAGE-OFF$
class SubmissionProcessingCommandsSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 109

  final val TrackProgressManifest  = classOf[TrackProgress].getName
  final val StartUploadManifest    = classOf[StartUpload].getName
  final val CompleteUploadManifest = classOf[CompleteUpload].getName
  final val StartParsingManifest   = classOf[StartParsing].getName
  final val PersistHmdaRowParsedErrorManifest =
    classOf[PersistHmdaRowParsedError].getName
  final val GetParsedWithErrorCountManifest =
    classOf[GetParsedWithErrorCount].getName
  final val GetParsingErrorsManifest = classOf[GetParsingErrors].getName
  final val CompleteParsingManifest  = classOf[CompleteParsing].getName
  final val CompleteParsingWithErrorsManifest =
    classOf[CompleteParsingWithErrors].getName
  final val StartSyntacticalValidityManifest =
    classOf[StartSyntacticalValidity].getName
  final val PersistHmdaRowValidatedErrorManifest =
    classOf[PersistHmdaRowValidatedError].getName
  final val PersistMacroErrorManifest =
    classOf[PersistMacroError].getName
  final val GetHmdaValidationErrorStateManifest =
    classOf[GetHmdaValidationErrorState].getName
  final val CompleteSyntacticalValidityManifest =
    classOf[CompleteSyntacticalValidity].getName
  final val StartQualityManifest =
    classOf[StartQuality].getName
  final val StartMacroManifest =
    classOf[StartMacro].getName
  final val CompleteQualityManifest =
    classOf[CompleteQuality].getName
  final val VerifyQualityManifest =
    classOf[VerifyQuality].getName
  final val VerifyMacroManifest =
    classOf[VerifyMacro].getName
  final val SignSubmissionManifest        = classOf[SignSubmission].getName
  final val GetVerificationStatusManifest = classOf[GetVerificationStatus].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {

    case cmd: TrackProgress =>
      trackProgressToProtobuf(cmd, resolver).toByteArray
    case cmd: StartUpload =>
      startUploadToProtobuf(cmd).toByteArray
    case cmd: CompleteUpload =>
      completeUploadToProtobuf(cmd).toByteArray
    case cmd: StartParsing =>
      startParsingToProtobuf(cmd).toByteArray
    case cmd: PersistHmdaRowParsedError =>
      persistHmdaRowParsedErrorToProtobuf(cmd, resolver).toByteArray
    case cmd: GetParsedWithErrorCount =>
      getParsedWithErrorCountToProtobuf(cmd, resolver).toByteArray
    case cmd: GetParsingErrors =>
      getParsingErrorsToProtobuf(cmd, resolver).toByteArray
    case cmd: CompleteParsing =>
      completeParsingToProtobuf(cmd).toByteArray
    case cmd: CompleteParsingWithErrors =>
      completeParsingWithErrorsToProtobuf(cmd).toByteArray
    case cmd: StartSyntacticalValidity =>
      startSyntacticalValidityToProtobuf(cmd).toByteArray
    case cmd: PersistHmdaRowValidatedError =>
      persistHmdaRowValidatedErrorToProtobuf(cmd, resolver).toByteArray
    case cmd: PersistMacroError =>
      persisteMacroErrorToProtobuf(cmd, resolver).toByteArray
    case cmd: GetHmdaValidationErrorState =>
      getHmdaValidationErrorStateToProtobuf(cmd, resolver).toByteArray
    case cmd: CompleteSyntacticalValidity =>
      completeSyntacticalValidityToProtobuf(cmd).toByteArray
    case cmd: StartQuality =>
      startQualityToProtobuf(cmd).toByteArray
    case cmd: StartMacro =>
      startMacroToProtobuf(cmd).toByteArray
    case cmd: CompleteQuality =>
      completeQualityToProtobuf(cmd).toByteArray
    case cmd: VerifyQuality =>
      verifyQualityToProtobuf(cmd, resolver).toByteArray
    case cmd: VerifyMacro =>
      verifyMacroToProtobuf(cmd, resolver).toByteArray
    case cmd: SignSubmission =>
      signSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: GetVerificationStatus =>
      getVerificationStatusToProtobuf(cmd, resolver).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case TrackProgressManifest =>
        trackProgressFromProtobuf(TrackProgressMessage.parseFrom(bytes), resolver)
      case StartUploadManifest =>
        startUploadFromProtobuf(StartUploadMessage.parseFrom(bytes))
      case CompleteUploadManifest =>
        completeUploadFromProtobuf(CompleteUploadMessage.parseFrom(bytes))
      case StartParsingManifest =>
        startParsingFromProtobuf(StartParsingMessage.parseFrom(bytes))
      case PersistHmdaRowParsedErrorManifest =>
        persistHmdaRowParsedErrorFromProtobuf(PersistHmdaRowParsedErrorMessage.parseFrom(bytes), resolver)
      case GetParsedWithErrorCountManifest =>
        getParsedWithErrorCountFromProtobuf(GetParsedWithErrorCountMessage.parseFrom(bytes), resolver)
      case GetParsingErrorsManifest =>
        getParsingErrorsFromProtobuf(GetParsingErrorsMessage.parseFrom(bytes), resolver)
      case CompleteParsingManifest =>
        completeParsingFromProtobuf(CompleteParsingMessage.parseFrom(bytes))
      case CompleteParsingWithErrorsManifest =>
        completeParsingWithErrorsFromProtobuf(CompleteParsingWithErrorsMessage.parseFrom(bytes))
      case StartSyntacticalValidityManifest =>
        startSyntacticalValidityFromProtobuf(StartSyntacticalValidityMessage.parseFrom(bytes))
      case PersistHmdaRowValidatedErrorManifest =>
        persistHmdaRowValidatedErrorFromProtobuf(PersistHmdaRowValidatedErrorMessage.parseFrom(bytes), resolver)
      case PersistMacroErrorManifest =>
        persistMacroErrorFromProtobuf(PersistMacroErrorMessage.parseFrom(bytes), resolver)
      case GetHmdaValidationErrorStateManifest =>
        getHmdaValidationErrorStateFromProtobuf(GetHmdaValidationErrorStateMessage.parseFrom(bytes), resolver)
      case CompleteSyntacticalValidityManifest =>
        completeSyntacticalValidityFromProtobuf(CompleteSyntacticalValidityMessage.parseFrom(bytes))
      case StartQualityManifest =>
        startQualituFromProtobuf(StartQualityMessage.parseFrom(bytes))
      case StartMacroManifest =>
        startMacroFromProtobuf(StartMacroMessage.parseFrom(bytes))
      case CompleteQualityManifest =>
        completeQualityFromProtobuf(CompleteQualityMessage.parseFrom(bytes))
      case VerifyQualityManifest =>
        verifyQualityFromProtobuf(VerifyQualityMessage.parseFrom(bytes), resolver)
      case VerifyMacroManifest =>
        verifyMacroFromProtobuf(VerifyMacroMessage.parseFrom(bytes), resolver)
      case SignSubmissionManifest =>
        signSubmissionFromProtobuf(SignSubmissionMessage.parseFrom(bytes), resolver)
      case GetVerificationStatusManifest =>
        getVerificationStatusFromProtobuf(GetVerificationStatusMessage.parseFrom(bytes), resolver)
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
// $COVERAGE-ON$
