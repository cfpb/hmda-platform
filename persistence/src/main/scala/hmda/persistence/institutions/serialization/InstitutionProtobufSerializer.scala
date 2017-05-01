package hmda.persistence.institutions.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.institutions.serialization.InstitutionProtobufConverter._
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.serialization.InstitutionEvents.{ InstitutionCreatedMessage, InstitutionModifiedMessage }

class InstitutionProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1000

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val InstitutionCreatedManifest = classOf[InstitutionCreated].getName
  final val InstitutionModifiedManifest = classOf[InstitutionModified].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: InstitutionCreated => institutionCreatedToProtobuf(evt).toByteArray
    case evt: InstitutionModified => institutionModifiedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case InstitutionCreatedManifest =>
      institutionCreatedFromProtobuf(InstitutionCreatedMessage.parseFrom(bytes))
    case InstitutionModifiedManifest =>
      institutionModifiedFromProtobuf(InstitutionModifiedMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
