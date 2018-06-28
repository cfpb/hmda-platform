package hmda.persistence.serialization.institution

import org.scalatest.{BeforeAndAfterAll, MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.institution.InstitutionPersistence.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified,
  InstitutionNotExists
}

class InstitutionEventsSerializerSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with BeforeAndAfterAll {

  val serializer = new InstitutionEventsSerializer()

  property("Institution Evetns must serialize to and from binary") {
    forAll(institutionGen) { institution =>
      val created = InstitutionCreated(institution)
      val modified = InstitutionModified(institution)
      val deleted = InstitutionDeleted(institution.LEI.getOrElse(""))
      val bytesCreated = serializer.toBinary(created)
      val bytesModified = serializer.toBinary(modified)
      val bytesDeleted = serializer.toBinary(deleted)
      val bytesNotExist = serializer.toBinary(InstitutionNotExists)
      serializer.fromBinary(bytesCreated, serializer.InstitutionCreatedManifest) mustBe created
      serializer.fromBinary(
        bytesModified,
        serializer.InstitutionModifiedManifest) mustBe modified
      serializer.fromBinary(bytesDeleted, serializer.InstitutionDeletedManifest) mustBe deleted
      serializer.fromBinary(
        bytesNotExist,
        serializer.InstitutionNotExistsManifest) mustBe InstitutionNotExists
    }
  }

}
