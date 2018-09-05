package hmda.serialization.institution

import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified,
  InstitutionNotExists
}
import hmda.persistence.serialization.institution.events.{
  InstitutionCreatedMessage,
  InstitutionDeletedMessage,
  InstitutionModifiedMessage,
  InstitutionNotExistsMessage
}

import hmda.model.institution.InstitutionGenerators._
import hmda.serialization.institution.InstitutionEventsProtobufConverter._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class InstitutionEventsProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("InstitutionCreated must convert to and protobuf") {
    forAll(institutionGen) { institution =>
      val created = InstitutionCreated(institution)
      val protobuf = institutionCreatedToProtobuf(created).toByteArray
      institutionCreatedFromProtobuf(
        InstitutionCreatedMessage.parseFrom(protobuf)) mustBe created
    }
  }

  property("InstitutionModified must convert to and protobuf") {
    forAll(institutionGen) { institution =>
      val modified = InstitutionModified(institution)
      val protobuf = institutionModifiedToProtobuf(modified).toByteArray
      institutionModifiedFromProtobuf(
        InstitutionModifiedMessage.parseFrom(protobuf)) mustBe modified
    }

  }

  property("InstitutionDeleted must convert to and protobuf") {
    forAll(institutionGen) { institution =>
      val lei = institution.LEI
      val deleted = InstitutionDeleted(lei)
      val protobuf = institutionDeletedToProtobuf(deleted).toByteArray
      institutionDeletedFromProtobuf(
        InstitutionDeletedMessage.parseFrom(protobuf)) mustBe deleted
    }
  }

  property("InstitutionNotExists must convert to and protobuf") {
    forAll(institutionGen) { institution =>
      val protobuf =
        institutionNotExistsToProtobuf(InstitutionNotExists(institution.LEI)).toByteArray
      institutionNotExistsFromProtobuf(
        InstitutionNotExistsMessage
          .parseFrom(protobuf)) mustBe InstitutionNotExists(institution.LEI)
    }
  }

}
