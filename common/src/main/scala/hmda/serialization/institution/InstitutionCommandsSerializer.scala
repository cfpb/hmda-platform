package hmda.serialization.institution

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter._
import akka.serialization.SerializerWithStringManifest
import hmda.messages.institution.InstitutionCommands.{ GetInstitutionDetails, _ }
import hmda.model.institution.Institution
import hmda.persistence.serialization.institution.InstitutionMessage
import hmda.serialization.institution.InstitutionCommandsProtobufConverter._
import hmda.serialization.institution.InstitutionProtobufConverter._
// $COVERAGE-OFF$
class InstitutionCommandsSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 101

  final val InstitutionManifest       = classOf[Institution].getName
  final val CreateInstitutionManifest = classOf[CreateInstitution].getName
  final val ModifyInstitutionManifest = classOf[ModifyInstitution].getName
  final val GetInstitutionManifest    = classOf[GetInstitution].getName
  final val GetInstitutionDetailsManifest =
    classOf[GetInstitutionDetails].getName
  final val DeleteInstitutionManifest = classOf[DeleteInstitution].getName
  final val AddFilingManifest         = classOf[AddFiling].getName

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
    case cmd: GetInstitutionDetails =>
      getInstitutionDetailsToProtobuf(cmd, resolver).toByteArray
    case cmd: DeleteInstitution =>
      deleteInstitutionToProtobuf(cmd, resolver).toByteArray
    case cmd: AddFiling =>
      addFilingToProtobuf(cmd, resolver).toByteArray
    case _ ⇒
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case InstitutionManifest =>
        institutionFromProtobuf(InstitutionMessage.parseFrom(bytes))
      case CreateInstitutionManifest =>
        createInstitutionFromProtobuf(bytes, resolver)
      case ModifyInstitutionManifest =>
        modifyInstitutionFromProtobuf(bytes, resolver)
      case GetInstitutionManifest =>
        getInstitutionFromProtobuf(bytes, resolver)
      case GetInstitutionDetailsManifest =>
        getInstitutionDetailsFromProtobuf(bytes, resolver)
      case DeleteInstitutionManifest =>
        deleteInstitutionFromProtobuf(bytes, resolver)
      case AddFilingManifest =>
        addFilingFromProtobuf(bytes, resolver)
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
// $COVERAGE-OFF$