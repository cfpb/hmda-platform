package hmda.serialization.institution

import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified,
  InstitutionNotExists
}
import hmda.model.institution.InstitutionGenerators._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{BeforeAndAfterAll, MustMatchers, PropSpec}

class InstitutionEventsSerializerSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with BeforeAndAfterAll {

  val serializer = new InstitutionEventsSerializer()

  property("InstitutionCreated must serialize to and from binary") {
    forAll(institutionGen) { institution =>
      val created = InstitutionCreated(institution)
      val bytesCreated = serializer.toBinary(created)
      serializer.fromBinary(bytesCreated, serializer.InstitutionCreatedManifest) mustBe created
    }
  }

  property("InstitutionModified must serialize to and from binary") {
    forAll(institutionGen) { institution =>
      val modified = InstitutionModified(institution)
      val bytesModified = serializer.toBinary(modified)
      serializer.fromBinary(
        bytesModified,
        serializer.InstitutionModifiedManifest) mustBe modified
    }
  }

  property("InstitutionDeleted must serialize to and from binary") {
    forAll(institutionGen) { institution =>
      val deleted = InstitutionDeleted(institution.LEI)
      val bytesDeleted = serializer.toBinary(deleted)
      serializer.fromBinary(bytesDeleted, serializer.InstitutionDeletedManifest) mustBe deleted
    }
  }

  property("InstitutionNotExists must serialize to and from binary") {
    forAll(institutionGen) { institution =>
      val notExists = InstitutionNotExists(institution.LEI)
      val bytesNotExist = serializer.toBinary(notExists)
      serializer.fromBinary(
        bytesNotExist,
        serializer.InstitutionNotExistsManifest) mustBe InstitutionNotExists(
        institution.LEI)
    }
  }

}
