package hmda.persistence.processing.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.LineAddedMessage
import hmda.persistence.processing.HmdaRawFile.LineAdded

class HmdaRawFileProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 9001

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val LineAddedManifest = classOf[LineAdded].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case LineAddedManifest =>
      val lineAddedMessage = LineAddedMessage.parseFrom(bytes)
      LineAdded(lineAddedMessage.timestamp, lineAddedMessage.data)


  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case LineAdded(ts, data) =>
      LineAddedMessage(ts, data).toByteArray

  }

}
