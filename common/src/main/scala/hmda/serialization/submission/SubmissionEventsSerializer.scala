package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionModified, SubmissionNotExists }
import hmda.persistence.serialization.submission.events.{ SubmissionCreatedMessage, SubmissionModifiedMessage, SubmissionNotExistsMessage }
import hmda.serialization.submission.SubmissionEventsProtobufConverter._

class SubmissionEventsSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 104

  final val SubmissionCreatedManifest   = classOf[SubmissionCreated].getName
  final val SubmissionModifiedManifest  = classOf[SubmissionModified].getName
  final val SubmissionNotExistsManifest = classOf[SubmissionNotExists].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: SubmissionCreated =>
      submissionCreatedToProtobuf(evt).toByteArray
    case evt: SubmissionModified =>
      submissionModifiedToProtobuf(evt).toByteArray
    case evt: SubmissionNotExists =>
      submissionNotExistsToProtobuf(evt).toByteArray
    case _ ⇒
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case SubmissionCreatedManifest =>
        submissionCreatedFromProtobuf(SubmissionCreatedMessage.parseFrom(bytes))
      case SubmissionModifiedManifest =>
        submissionModifiedFromProtobuf(SubmissionModifiedMessage.parseFrom(bytes))
      case SubmissionNotExistsManifest =>
        submissionNotExistsFromProtobuf(SubmissionNotExistsMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
