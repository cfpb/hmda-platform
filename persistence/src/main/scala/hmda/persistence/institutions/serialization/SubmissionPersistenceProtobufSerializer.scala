package hmda.persistence.institutions.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.model.submission.SubmissionMessage
import hmda.persistence.institutions.SubmissionPersistence.{ SubmissionCreated, SubmissionStatusUpdated }
import hmda.persistence.institutions.serialization.SubmissionConverter._
import hmda.persistence.messages.{ SubmissionCreatedMessage, SubmissionStatusUpdatedMessage }

class SubmissionPersistenceProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 8003

  override def manifest(o: AnyRef): String = o.getClass.getName
  final val SubmissionCreatedManifest = classOf[SubmissionCreated].getName
  final val SubmissionStatusUpdatedManifest = classOf[SubmissionStatusUpdated].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case SubmissionCreatedManifest =>
      val submissionCreatedMessage = SubmissionMessage.parseFrom(bytes)
      SubmissionCreated(Some(submissionCreatedMessage))

    case SubmissionStatusUpdatedManifest =>
      val submissionStatusUpdatedMessage = SubmissionMessage.parseFrom(bytes)
      val submissionId = submissionStatusUpdatedMessage.submissionId
      val status = submissionStatusUpdatedMessage.submissionStatus
      SubmissionStatusUpdated(submissionId, status)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case SubmissionCreated(submission) =>
      SubmissionCreatedMessage(submission).toByteArray
    case SubmissionStatusUpdated(submissionId, status) =>
      SubmissionStatusUpdatedMessage(submissionId, status).toByteArray
  }

}
