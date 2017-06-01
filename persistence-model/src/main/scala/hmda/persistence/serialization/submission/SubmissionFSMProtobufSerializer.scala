package hmda.persistence.serialization.submission

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.processing.SubmissionFSMEvents._
import hmda.persistence.model.serialization.SubmissionFSM._
import hmda.persistence.serialization.submission.SubmissionFSMProtobufConverter._

class SubmissionFSMProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1009

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val SubmissionFSMCreatedManifest = classOf[SubmissionFSMCreated].getName
  final val SubmissionUploadingManifest = classOf[SubmissionUploading].getName
  final val SubmissionUploadedManifest = classOf[SubmissionUploaded].getName
  final val SubmissionParsingManifest = classOf[SubmissionParsing].getName
  final val SubmissionParsedManifest = classOf[SubmissionParsed].getName
  final val SubmissionParsedWithErrorsManifest = classOf[SubmissionParsedWithErrors].getName
  final val SubmissionValidatingManifest = classOf[SubmissionValidating].getName
  final val SubmissionValidatedManifest = classOf[SubmissionValidated].getName
  final val SubmissionValidatedWithErrorsManifest = classOf[SubmissionValidatedWithErrors].getName
  final val SubmissionSignedManifest = classOf[SubmissionSigned].getName
  final val SubmissionFailedManifest = classOf[SubmissionFailed].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: SubmissionFSMCreated => submissionFSMCreatedToProtobuf(evt).toByteArray
    case evt: SubmissionUploading => submissionUploadingToProtobuf(evt).toByteArray
    case evt: SubmissionUploaded => submissionUploadedToProtobuf(evt).toByteArray
    case evt: SubmissionParsing => submissionParsingToProtobuf(evt).toByteArray
    case evt: SubmissionParsed => submissionParsedToProtobuf(evt).toByteArray
    case evt: SubmissionParsedWithErrors => submissionParsedWithErrorsToProtobuf(evt).toByteArray
    case evt: SubmissionValidating => submissionValidatingToProtobuf(evt).toByteArray
    case evt: SubmissionValidated => submissionValidatedToProtobuf(evt).toByteArray
    case evt: SubmissionValidatedWithErrors => submissionValidatedWithErrorsToProtobuf(evt).toByteArray
    case evt: SubmissionSigned => submissionSignedToProtobuf(evt).toByteArray
    case evt: SubmissionFailed => submissionFailedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case SubmissionFSMCreatedManifest =>
      submissionFSMCreatedFromProtobuf(SubmissionFSMCreatedMessage.parseFrom(bytes))
    case SubmissionUploadingManifest =>
      submissionUploadingFromProtobuf(SubmissionUploadingMessage.parseFrom(bytes))
    case SubmissionUploadedManifest =>
      submissionUploadedFromProtobuf(SubmissionUploadedMessage.parseFrom(bytes))
    case SubmissionParsingManifest =>
      submissionParsingFromProtobuf(SubmissionParsingMessage.parseFrom(bytes))
    case SubmissionParsedManifest =>
      submissionParsedFromProtobuf(SubmissionParsedMessage.parseFrom(bytes))
    case SubmissionParsedWithErrorsManifest =>
      submissionParsedWithErrorsFromProtobuf(SubmissionParsedWithErrorsMessage.parseFrom(bytes))
    case SubmissionValidatingManifest =>
      submissionValidatingFromProtobuf(SubmissionValidatingMessage.parseFrom(bytes))
    case SubmissionValidatedManifest =>
      submissionValidatedFromProtobuf(SubmissionValidatedMessage.parseFrom(bytes))
    case SubmissionValidatedWithErrorsManifest =>
      submissionValidatedWithErrorsFromProtobuf(SubmissionValidatedWithErrorsMessage.parseFrom(bytes))
    case SubmissionSignedManifest =>
      submissionSignedFromProtobuf(SubmissionSignedMessage.parseFrom(bytes))
    case SubmissionFailedManifest =>
      submissionFailedFromProtobuf(SubmissionFailedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
