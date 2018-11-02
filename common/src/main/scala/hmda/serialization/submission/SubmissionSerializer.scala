package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.model.filing.submission.Submission
import hmda.persistence.serialization.submission.SubmissionMessage
import hmda.serialization.submission.SubmissionProtobufConverter._

class SubmissionSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 110

  final val SubmissionManifest = classOf[Submission].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case s: Submission =>
      submissionToProtobuf(s).toByteArray
    case _ ⇒
      throw new IllegalArgumentException(
        s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case SubmissionManifest =>
        submissionFromProtobuf(SubmissionMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
