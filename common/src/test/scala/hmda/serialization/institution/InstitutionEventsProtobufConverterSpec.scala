package hmda.serialization.institution

import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionDeleted, InstitutionModified, InstitutionNotExists }
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.serialization.institution.events.{
  InstitutionCreatedMessage,
  InstitutionDeletedMessage,
  InstitutionModifiedMessage,
  InstitutionNotExistsMessage
}
import hmda.serialization.institution.InstitutionEventsProtobufConverter._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class InstitutionEventsProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("InstitutionCreated must convert to and from protobuf") {
    forAll(institutionGen) { institution =>
      val created  = InstitutionCreated(institution)
      val protobuf = institutionCreatedToProtobuf(created).toByteArray
      institutionCreatedFromProtobuf(InstitutionCreatedMessage.parseFrom(protobuf)) mustBe created
    }
  }

  property("InstitutionModified must convert to and from protobuf") {
    forAll(institutionGen) { institution =>
      val modified = InstitutionModified(institution)
      val protobuf = institutionModifiedToProtobuf(modified).toByteArray
      institutionModifiedFromProtobuf(InstitutionModifiedMessage.parseFrom(protobuf)) mustBe modified
    }

  }

  property("InstitutionDeleted must convert to and from protobuf") {
    forAll(institutionGen) { institution =>
      val lei      = institution.LEI
      val deleted  = InstitutionDeleted(lei, institution.activityYear)
      val protobuf = institutionDeletedToProtobuf(deleted).toByteArray
      institutionDeletedFromProtobuf(InstitutionDeletedMessage.parseFrom(protobuf)) mustBe deleted
    }
  }

  property("InstitutionNotExists must convert to and from protobuf") {
    forAll(institutionGen) { institution =>
      val protobuf =
        institutionNotExistsToProtobuf(InstitutionNotExists(institution.LEI)).toByteArray
      institutionNotExistsFromProtobuf(
        InstitutionNotExistsMessage
          .parseFrom(protobuf)
      ) mustBe InstitutionNotExists(institution.LEI)
    }
  }

}