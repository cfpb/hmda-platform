package hmda.persistence.serialization.institutions

import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.messages.commands.institutions.InstitutionCommands._
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import org.scalacheck.Gen
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

  property("Get Institution By Respondent Id messages must be serialized to binary and back") {
    forAll(institutionGen) { institution =>
      val msg = GetInstitutionByRespondentId(institution.id)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.GetInstitutionByRespondentIdManifest) mustBe GetInstitutionByRespondentId(institution.id)
    }
  }

  property("Get Institution By Id messages must be serialized to binary and back") {
    forAll(institutionGen) { institution =>
      val msg = GetInstitutionById(institution.id)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.GetInstitutionByIdManifest) mustBe GetInstitutionById(institution.id)
    }
  }

  property("Get Institutions By Id messages must be serialized to binary and back") {
    forAll(Gen.listOfN(5, Gen.alphaStr)) { list =>
      val msg = GetInstitutionsById(list)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.GetInstitutionsByIdManifest) mustBe GetInstitutionsById(list)
    }
  }

  property("Find Institution By Domain messages must be serialized to binary and back") {
    forAll(Gen.alphaStr) { domain =>
      val msg = FindInstitutionByDomain(domain)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.FindInstitutionByDomainManifest) mustBe FindInstitutionByDomain(domain)
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
