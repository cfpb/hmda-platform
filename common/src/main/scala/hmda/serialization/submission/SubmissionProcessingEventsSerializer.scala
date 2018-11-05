package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError
}
import SubmissionProcessingEventsProtobufConverter._
import hmda.persistence.serialization.submission.processing.events.{
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage
}

class SubmissionProcessingEventsSerializer
    extends SerializerWithStringManifest {
  override def identifier: Int = 108

  final val ParsedErrorManifest = classOf[HmdaRowParsedError].getName
  final val ParsedErrorCountManifest = classOf[HmdaRowParsedCount].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: HmdaRowParsedError =>
      hmdaRowParsedErrorToProtobuf(evt).toByteArray
    case evt: HmdaRowParsedCount =>
      hmdaRowParsedCountToProtobuf(evt).toByteArray
    case _ ⇒
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
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
