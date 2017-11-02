package hmda.persistence.serialization.institutions

import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.messages.commands.institutions.InstitutionCommands.{ CreateInstitution, ModifyInstitution }
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class InstitutionProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {

  val serializer = new InstitutionProtobufSerializer()

  property("Institution Created messages must be serialized to binary and back") {
    forAll(institutionGen) { institution =>
      val msg = InstitutionCreated(institution)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.InstitutionCreatedManifest) mustBe InstitutionCreated(institution)
    }
  }

  property("Institution Modified messages must be serialized to binary and back") {
    forAll(institutionGen) { institution =>
      val msg = InstitutionModified(institution)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.InstitutionModifiedManifest) mustBe InstitutionModified(institution)
    }
  }

  property("Institution messages must be serialized to binary and back") {
    forAll(institutionGen) { institution =>
      val bytes = serializer.toBinary(institution)
      serializer.fromBinary(bytes, serializer.InstitutionManifest) mustBe institution
    }
  }

  property("Create Institution messages must be serialized to binary and back") {
    forAll(institutionGen) { institution =>
      val msg = CreateInstitution(institution)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.CreateInstitutionManifest) mustBe CreateInstitution(institution)
    }
  }

  property("Modify Institution messages must be serialized to binary and back") {
    forAll(institutionGen) { institution =>
      val msg = ModifyInstitution(institution)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.ModifyInstitutionManifest) mustBe ModifyInstitution(institution)
    }
  }
}