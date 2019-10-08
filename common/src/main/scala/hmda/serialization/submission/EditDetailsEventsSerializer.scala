package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.EditDetailsEvents.{ EditDetailsAdded, EditDetailsRowCounted }
import hmda.persistence.serialization.edit.details.events.{ EditDetailsAddedMessage, EditDetailsRowCountedMessage }
import hmda.serialization.submission.EditDetailsEventsProtobufConverter._

class EditDetailsEventsSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 113

  final val EditDetailsAddedManifest = classOf[EditDetailsAdded].getName
  final val EditDetailsRowCountedManifest =
    classOf[EditDetailsRowCounted].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: EditDetailsAdded =>
      editDetailsAddedToProtobuf(evt).toByteArray
    case evt: EditDetailsRowCounted =>
      editDetailsRowCountedToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case EditDetailsAddedManifest =>
        editDetailsAddedFromProtobuf(EditDetailsAddedMessage.parseFrom(bytes))
      case EditDetailsRowCountedManifest =>
        editDetailsRowCountedFromProtobuf(EditDetailsRowCountedMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
