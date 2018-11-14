package hmda.serialization.submission

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.serialization.SerializerWithStringManifest
import akka.actor.typed.scaladsl.adapter._
import hmda.messages.submission.SubmissionProcessingCommands.{
  GetParsingErrors,
  _
}
import SubmissionProcessingCommandsProtobufConverter._
import hmda.persistence.serialization.submission.processing.commands._

class SubmissionProcessingCommandsSerializer(system: ExtendedActorSystem)
    extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 109

  final val StartUploadManifest = classOf[StartUpload].getName
  final val CompleteUploadManifest = classOf[CompleteUpload].getName
  final val StartParsingManifest = classOf[StartParsing].getName
  final val PersistHmdaRowParsedErrorManifest =
    classOf[PersistHmdaRowParsedError].getName
  final val GetParsedWithErrorCountManifest =
    classOf[GetParsedWithErrorCount].getName
  final val GetParsingErrorsManifest = classOf[GetParsingErrors].getName
  final val CompleteParsingManifest = classOf[CompleteParsing].getName
  final val CompleteParsingWithErrorsManifest =
    classOf[CompleteParsingWithErrors].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {

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
    case _ =>
      throw new IllegalArgumentException(
        s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case StartUploadManifest =>
        startUploadFromProtobuf(StartUploadMessage.parseFrom(bytes))
      case CompleteUploadManifest =>
        completeUploadFromProtobuf(CompleteUploadMessage.parseFrom(bytes))
      case StartParsingManifest =>
        startParsingFromProtobuf(StartParsingMessage.parseFrom(bytes))
      case PersistHmdaRowParsedErrorManifest =>
        persistHmdaRowParsedErrorFromProtobuf(
          PersistHmdaRowParsedErrorMessage.parseFrom(bytes),
          resolver)
      case GetParsedWithErrorCountManifest =>
        getParsedWithErrorCountFromProtobuf(
          GetParsedWithErrorCountMessage.parseFrom(bytes),
          resolver)
      case GetParsingErrorsManifest =>
        getParsingErrorsFromProtobuf(GetParsingErrorsMessage.parseFrom(bytes),
                                     resolver)
      case CompleteParsingManifest =>
        completeParsingFromProtobuf(CompleteParsingMessage.parseFrom(bytes))
      case CompleteParsingWithErrorsManifest =>
        completeParsingWithErrorsFromProtobuf(
          CompleteParsingWithErrorsMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
