package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError,
  HmdaRowValidatedError,
  PersistedHmdaRowParsedError
}
import SubmissionProcessingEventsProtobufConverter._
import hmda.serialization.validation.ValidationProtobufConverter._
import hmda.model.processing.state.{
  HmdaParserErrorState,
  HmdaValidationErrorState
}
import hmda.persistence.serialization.submission.processing.commands.HmdaValidationErrorStateMessage
import hmda.persistence.serialization.submission.processing.events._

class SubmissionProcessingEventsSerializer
    extends SerializerWithStringManifest {
  override def identifier: Int = 108

  final val ParsedErrorManifest = classOf[HmdaRowParsedError].getName
  final val ParsedErrorCountManifest = classOf[HmdaRowParsedCount].getName
  final val HmdaParserErrorStateManifest = classOf[HmdaParserErrorState].getName
  final val PersistedHmdaRowParsedErrorManifest =
    classOf[PersistedHmdaRowParsedError].getName
  final val HmdaValidationErrorStateManifest =
    classOf[HmdaValidationErrorState].getName
  final val HmdaRowValidatedErrorManifest =
    classOf[HmdaRowValidatedError].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: HmdaRowParsedError =>
      hmdaRowParsedErrorToProtobuf(evt).toByteArray
    case evt: HmdaRowParsedCount =>
      hmdaRowParsedCountToProtobuf(evt).toByteArray
    case evt: HmdaParserErrorState =>
      hmdaParserErrorStateToProtobuf(evt).toByteArray
    case evt: PersistedHmdaRowParsedError =>
      persistedHmdaRowParsedToProtobuf(evt).toByteArray
    case evt: HmdaValidationErrorState =>
      hmdaValidationErrorStateToProtobuf(evt).toByteArray
    case evt: HmdaRowValidatedError =>
      hmdaRowValidatedErrorToProtobuf(evt).toByteArray
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
      case PersistedHmdaRowParsedErrorManifest =>
        persistedHmdaRowParsedFromProtobuf(
          PersistedHmdaRowParsedErrorMessage.parseFrom(bytes))
      case HmdaValidationErrorStateManifest =>
        hmdaValidationErrorStateFromProtobuf(
          HmdaValidationErrorStateMessage.parseFrom(bytes))
      case HmdaRowValidatedErrorManifest =>
        hmdaRowValidatedErrorFromProtobuf(
          HmdaRowValidatedErrorMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
