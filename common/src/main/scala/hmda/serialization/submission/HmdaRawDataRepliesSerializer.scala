package hmda.serialization.submission

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.HmdaRawDataReplies.LinesAdded
import hmda.persistence.serialization.raw.data.replies.LinesAddedMessage
import hmda.serialization.submission.HmdaRawDataRepliesProtobufConverter._

import java.io.NotSerializableException

class HmdaRawDataRepliesSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 116

  final val LinesAddedManifest = classOf[LinesAdded].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: LinesAdded =>
      linesAddedToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case LinesAddedManifest =>
        linesAddedFromProtobuf(LinesAddedMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}