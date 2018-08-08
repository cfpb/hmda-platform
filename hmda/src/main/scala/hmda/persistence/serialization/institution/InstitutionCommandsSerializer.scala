package hmda.persistence.serialization.institution

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.serialization.SerializerWithStringManifest
import akka.actor.typed.scaladsl.adapter._
import InstitutionProtobufConverter._
import InstitutionCommandsProtobufConverter._
import hmda.messages.institution.InstitutionCommands.{
  CreateInstitution,
  DeleteInstitution,
  GetInstitution,
  ModifyInstitution
}
import hmda.model.institution.Institution

class InstitutionCommandsSerializer(system: ExtendedActorSystem)
    extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 101

  final val InstitutionManifest = classOf[Institution].getName
  final val CreateInstitutionManifest = classOf[CreateInstitution].getName
  final val ModifyInstitutionManifest = classOf[ModifyInstitution].getName
  final val GetManifest = classOf[GetInstitution].getName
  final val DeleteInstitutionManifest = classOf[DeleteInstitution].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case i: Institution =>
      institutionToProtobuf(i).toByteArray
    case cmd: CreateInstitution =>
      createInstitutionToProtobuf(cmd, resolver).toByteArray
    case cmd: ModifyInstitution =>
      modifyInstitutionToProtobuf(cmd, resolver).toByteArray
    case cmd: GetInstitution =>
      getInstitutionToProtobuf(cmd, resolver).toByteArray
    case cmd: DeleteInstitution =>
      deleteInstitutionToProtobuf(cmd, resolver).toByteArray
    case _ ⇒
      throw new IllegalArgumentException(
        s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case InstitutionManifest =>
        institutionFromProtobuf(InstitutionMessage.parseFrom(bytes))
      case CreateInstitutionManifest =>
        createInstitutionFromProtobuf(bytes, resolver)
      case ModifyInstitutionManifest =>
        modifyInstitutionFromProtobuf(bytes, resolver)
      case GetManifest =>
        getInstitutionFromProtobuf(bytes, resolver)
      case DeleteInstitutionManifest =>
        deleteInstitutionFromProtobuf(bytes, resolver)
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
