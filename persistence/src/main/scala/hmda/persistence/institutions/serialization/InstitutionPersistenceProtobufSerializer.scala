package hmda.persistence.institutions.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.institutions.InstitutionPersistence.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.messages.{ InstitutionCreatedMessage, InstitutionModifiedMessage }
import hmda.persistence.institutions.serialization.InstitutionConverter._

class InstitutionPersistenceProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 8002

  override def manifest(o: AnyRef): String = o.getClass.getName
  final val InstitutionCreatedManifest = classOf[InstitutionCreated].getName
  final val InstitutionModifiedManifest = classOf[InstitutionModified].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case InstitutionCreatedManifest =>
      val institutionCreatedMessage = InstitutionCreatedMessage.parseFrom(bytes)
      InstitutionCreated(institutionCreatedMessage.institution)
    case InstitutionModifiedManifest =>
      val institutionModifiedMessage = InstitutionModifiedMessage.parseFrom(bytes)
      InstitutionModified(institutionModifiedMessage.institution)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case InstitutionCreated(institution) =>
      InstitutionCreatedMessage(institution).toByteArray
    case InstitutionModified(institution) =>
      InstitutionModifiedMessage(institution).toByteArray
  }

}
