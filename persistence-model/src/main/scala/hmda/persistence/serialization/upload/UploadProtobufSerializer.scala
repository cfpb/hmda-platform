package hmda.persistence.serialization.upload

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.processing.FileUploadEvents.{ FileNameAdded, LineAdded }
import hmda.persistence.model.serialization.FileUpload.{ FileNameAddedMessage, LineAddedMessage }
import hmda.persistence.serialization.upload.UploadProtobufConverter._

class UploadProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1005

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val LineAddedManifest = classOf[LineAdded].getName
  final val FileNameAddedManifest = classOf[FileNameAdded].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: LineAdded => lineAddedToProtobuf(evt).toByteArray
    case evt: FileNameAdded => fileNameAddedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case LineAddedManifest =>
      lineAddedFromProtobuf(LineAddedMessage.parseFrom(bytes))
    case FileNameAddedManifest =>
      fileNameAddedFromProtobuf(FileNameAddedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
