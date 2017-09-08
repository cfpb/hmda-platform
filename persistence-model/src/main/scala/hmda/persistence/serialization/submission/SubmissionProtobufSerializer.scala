package hmda.persistence.serialization.submission

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.institutions.SubmissionEvents._
import hmda.persistence.model.serialization.SubmissionEvents._
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

class SubmissionProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1001

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val SubmissionCreatedManifest = classOf[SubmissionCreated].getName
  final val SubmissionStatusUpdatedManifest = classOf[SubmissionStatusUpdated].getName
  final val SubmissionFileNameAddedManifest = classOf[SubmissionFileNameAdded].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: SubmissionCreated => submissionCreatedToProtobuf(evt).toByteArray
    case evt: SubmissionStatusUpdated => submissionStatusUpdatedToProtobuf(evt).toByteArray
    case evt: SubmissionFileNameAdded => submissionFileNameAddedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case SubmissionCreatedManifest =>
      submissionCreatedFromProtobuf(SubmissionCreatedMessage.parseFrom(bytes))
    case SubmissionStatusUpdatedManifest =>
      submissionStatusUpdatedFromProtobuf(SubmissionStatusUpdatedMessage.parseFrom(bytes))
    case SubmissionFileNameAddedManifest =>
      submissionFileNameAddedFromProtobuf(SubmissionFileNameAddedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
