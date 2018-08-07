package hmda.persistence.serialization.institution

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import InstitutionEventsProtobufConverter._
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified,
  InstitutionNotExists
}
import hmda.persistence.serialization.institution.events.{
  InstitutionCreatedMessage,
  InstitutionDeletedMessage,
  InstitutionModifiedMessage,
  InstitutionNotExistsMessage
}

class InstitutionEventsSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 100

  final val InstitutionCreatedManifest =
    classOf[InstitutionCreated].getName

  final val InstitutionModifiedManifest =
    classOf[InstitutionModified].getName

  final val InstitutionDeletedManifest =
    classOf[InstitutionDeleted].getName

  final val InstitutionNotExistsManifest =
    classOf[InstitutionNotExists].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: InstitutionCreated =>
      institutionCreatedToProtobuf(evt).toByteArray
    case evt: InstitutionModified =>
      institutionModifiedToProtobuf(evt).toByteArray
    case evt: InstitutionDeleted =>
      institutionDeletedToProtobuf(evt).toByteArray
    case evt: InstitutionNotExists =>
      institutionNotExistsToProtobuf(evt).toByteArray
    case _ ⇒
      throw new IllegalArgumentException(
        s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case InstitutionCreatedManifest =>
        institutionCreatedFromProtobuf(
          InstitutionCreatedMessage.parseFrom(bytes))
      case InstitutionModifiedManifest =>
        institutionModifiedFromProtobuf(
          InstitutionModifiedMessage.parseFrom(bytes))
      case InstitutionDeletedManifest =>
        institutionDeletedFromProtobuf(
          InstitutionDeletedMessage.parseFrom(bytes))
      case InstitutionNotExistsManifest =>
        institutionNotExistsFromProtobuf(
          InstitutionNotExistsMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
