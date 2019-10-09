package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionManagerCommands.{ UpdateSubmissionStatus, WrappedSubmissionEventResponse }
import hmda.serialization.submission.SubmissionManagerCommandsProtobufConverter._

class SubmissionManagerCommandsSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 111

  final val UpdateSubmissionStatusManifest =
    classOf[UpdateSubmissionStatus].getName
  final val WrappedSubmissionEventResponseManifest =
    classOf[WrappedSubmissionEventResponse].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: UpdateSubmissionStatus =>
      updateSubmissionStatusToProtobuf(cmd).toByteArray
    case cmd: WrappedSubmissionEventResponse =>
      wrappedSubmissionEventResponseToProtobuf(cmd).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case UpdateSubmissionStatusManifest =>
        updateSubmissionStatusFromProtobuf(bytes)
      case WrappedSubmissionEventResponseManifest =>
        wrappedSubmissionEventResponseFromProtobuf(bytes)
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
