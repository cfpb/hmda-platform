package hmda.serialization.filing

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.filing.FilingEvents.{ FilingCreated, FilingStatusUpdated, SubmissionAdded, SubmissionUpdated }
import hmda.persistence.serialization.filing.events.{
  FilingCreatedMessage,
  FilingStatusUpdatedMessage,
  SubmissionAddedMessage,
  SubmissionUpdatedMessage
}
import hmda.serialization.filing.FilingEventsProtobufConverter._

class FilingEventsSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 106

  final val FilingCreatedManifest       = classOf[FilingCreated].getName
  final val FilingStatusUpdatedManifest = classOf[FilingStatusUpdated].getName
  final val SubmissionAddedManifest     = classOf[SubmissionAdded].getName
  final val SubmissionUpdatedManifest   = classOf[SubmissionUpdated].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: FilingCreated =>
      filingCreatedToProtobuf(evt).toByteArray
    case evt: FilingStatusUpdated =>
      filingStatusUpdatedToProtobuf(evt).toByteArray
    case evt: SubmissionAdded =>
      submissionAddedToProtobuf(evt).toByteArray
    case evt: SubmissionUpdated =>
      submissionUpdatedToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")

  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case FilingCreatedManifest =>
        filingCreatedFromProtobuf(FilingCreatedMessage.parseFrom(bytes))
      case FilingStatusUpdatedManifest =>
        filingStatusUpdatedFromProtobuf(FilingStatusUpdatedMessage.parseFrom(bytes))
      case SubmissionAddedManifest =>
        submissionAddedFromProtobuf(SubmissionAddedMessage.parseFrom(bytes))
      case SubmissionUpdatedManifest =>
        submissionUpdatedFromProtoubf(SubmissionUpdatedMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")

    }
}
