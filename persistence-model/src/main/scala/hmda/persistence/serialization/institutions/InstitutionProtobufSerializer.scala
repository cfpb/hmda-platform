package hmda.persistence.serialization.institutions

import akka.serialization.SerializerWithStringManifest
import hmda.model.institution.Institution
import hmda.persistence.messages.commands.commands.InstitutionCommands.{ CreateInstitution, ModifyInstitution }
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.serialization.InstitutionCommands.{ CreateInstitutionMessage, ModifyInstitutionMessage }
import hmda.persistence.model.serialization.InstitutionEvents.{ InstitutionCreatedMessage, InstitutionMessage, InstitutionModifiedMessage }
import hmda.persistence.serialization.institutions.InstitutionProtobufConverter._

class InstitutionProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1000

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val CreateInstitutionManifest = classOf[CreateInstitution].getName
  final val ModifyInstitutionManifest = classOf[ModifyInstitution].getName
  final val InstitutionCreatedManifest = classOf[InstitutionCreated].getName
  final val InstitutionModifiedManifest = classOf[InstitutionModified].getName
  final val InstitutionManifest = classOf[Institution].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: CreateInstitution => createInstitutionToProtobuf(cmd).toByteArray
    case cmd: ModifyInstitution => modifyInstitutionToProtobuf(cmd).toByteArray
    case evt: InstitutionCreated => institutionCreatedToProtobuf(evt).toByteArray
    case evt: InstitutionModified => institutionModifiedToProtobuf(evt).toByteArray
    case evt: Institution => institutionToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case CreateInstitutionManifest =>
      createInstitutionFromProtobuf(CreateInstitutionMessage.parseFrom(bytes))
    case ModifyInstitutionManifest =>
      modifyInstitutionFromProtobuf(ModifyInstitutionMessage.parseFrom(bytes))
    case InstitutionCreatedManifest =>
      institutionCreatedFromProtobuf(InstitutionCreatedMessage.parseFrom(bytes))
    case InstitutionModifiedManifest =>
      institutionModifiedFromProtobuf(InstitutionModifiedMessage.parseFrom(bytes))
    case InstitutionManifest =>
      institutionFromProtobuf(InstitutionMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
