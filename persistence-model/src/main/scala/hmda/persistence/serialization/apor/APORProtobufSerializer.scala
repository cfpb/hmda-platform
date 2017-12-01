package hmda.persistence.serialization.apor

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.commands.apor.APORCommands.CreateApor
import hmda.persistence.messages.events.apor.APOREvents.AporCreated
import hmda.persistence.model.serialization.APORCommands.CreateAPORMessage
import hmda.persistence.model.serialization.APOREvents.APORCreatedMessage
import hmda.persistence.serialization.apor.APORProtobufConverter._

class APORProtobufSerializer extends SerializerWithStringManifest {
  override def identifier = 1011

  override def manifest(o: AnyRef) = o.getClass.getName

  final val CreateAporManifest = classOf[CreateApor].getName
  final val AporCreatedManifest = classOf[AporCreated].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: CreateApor => createAporToProtobuf(cmd).toByteArray
    case evt: AporCreated => aporCreatedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case CreateAporManifest =>
      createAporFromProtobuf(CreateAPORMessage.parseFrom(bytes))
    case AporCreatedManifest =>
      aporCreatedFromProtobuf(APORCreatedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
