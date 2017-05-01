package hmda.persistence.institutions.serialization

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.institutions.serialization.InstitutionProtobufConverter._
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.serialization.InstitutionEvents._

class InstitutionProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {
  property("Top Holder must serialize to protobuf and back") {
    forAll(topHolderGen) { topHolder =>
      val protobuf = topHolderToProtobuf(topHolder).toByteArray
      topHolderFromProtobuf(TopHolderMessage.parseFrom(protobuf)) mustBe topHolder
    }
  }

  property("Parent must serialize to protobuf and back") {
    forAll(parentGen) { parent =>
      val protobuf = parentToProtobuf(parent).toByteArray
      parentFromProtobuf(ParentMessage.parseFrom(protobuf)) mustBe parent
    }
  }

  property("Respondent must serialize to protobuf and back") {
    forAll(respondentGen) { respondent =>
      val bytes = respondentToProtobud(respondent).toByteArray
      respondentFromProtobuf(RespondentMessage.parseFrom(bytes)) mustBe respondent
    }
  }

  property("External Id Type must serialize to protobuf and back") {
    forAll(externalIdTypeGen) { externalIdType =>
      val protobuf = externalIdTypeToProtobuf(externalIdType)
      externalIdTypeFromProtobuf(protobuf) mustBe externalIdType
    }
  }

  property("External Id must serialize to protobuf and back") {
    forAll(externalIdGen) { externalId =>
      val protobuf = externalIdToProtobuf(externalId).toByteArray
      externalIdFromProtobuf(ExternalIdMessage.parseFrom(protobuf)) mustBe externalId
    }
  }

  property("Institution Type must serialize to protobuf and back") {
    forAll(institutionTypeGen) { institutionType =>
      val protobuf = institutionTypeToProtobuf(institutionType)
      institutionTypeFromProtobuf(protobuf) mustBe institutionType
    }
  }

  property("Agency must serialize to protobuf and back") {
    forAll(agencyGen) { agency =>
      val protobuf = agencyToProtobuf(agency)
      agencyFromProtobuf(protobuf) mustBe agency
    }
  }

  property("Institution must serialize to protobuf and back") {
    forAll(institutionGen) { institution =>
      val protobuf = institutionToProtobuf(institution).toByteArray
      institutionFromProtobuf(InstitutionMessage.parseFrom(protobuf)) mustBe institution
    }
  }

  property("InstitutionCreated must serialize to protobuf and back") {
    forAll(institutionGen) { institution =>
      val protobuf = institutionCreatedToProtobuf(InstitutionCreated(institution)).toByteArray
      institutionCreatedFromProtobuf(InstitutionCreatedMessage.parseFrom(protobuf)) mustBe InstitutionCreated(institution)
    }
  }

  property("InstitutionModified must serialize to protobuf and back") {
    forAll(institutionGen) { institution =>
      val protobuf = institutionModifiedToProtobuf(InstitutionModified(institution)).toByteArray
      institutionModifiedFromProtobuf(InstitutionModifiedMessage.parseFrom(protobuf)) mustBe InstitutionModified(institution)
    }
  }

}
