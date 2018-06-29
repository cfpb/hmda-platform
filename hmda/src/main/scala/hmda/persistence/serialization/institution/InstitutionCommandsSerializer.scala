package hmda.persistence.serialization.institution

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.serialization.SerializerWithStringManifest
import hmda.persistence.institution.InstitutionPersistence.CreateInstitution
import akka.actor.typed.scaladsl.adapter._
import InstitutionCommandsProtobufConverter._

class InstitutionCommandsSerializer(system: ExtendedActorSystem)
    extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 101

  final val CreateInstitutionManifest = classOf[CreateInstitution].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: CreateInstitution =>
      createInstitutionToProtobuf(cmd, resolver).toByteArray
    case _ ⇒
      throw new IllegalArgumentException(
        s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case CreateInstitutionManifest =>
        createInstitutionFromProtobuf(bytes, resolver)
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
