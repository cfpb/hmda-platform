package hmda.serialization.institution

import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.serialization.institution.{ InstitutionMessage, ParentMessage, RespondentMessage, TopHolderMessage }
import hmda.serialization.institution.InstitutionProtobufConverter._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class InstitutionProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Top Holder must convert to and from protobuf") {
    forAll(topHolderGen) { topHolder =>
      val protobuf = topHolderToProtobuf(topHolder).toByteArray
      topHolderFromProtobuf(TopHolderMessage.parseFrom(protobuf)) mustBe topHolder
    }
  }

  property("Parent must convert to and from protobuf") {
    forAll(institutionParentGen) { parent =>
      val protobuf = parentToProtobuf(parent).toByteArray
      parentFromProtobuf(ParentMessage.parseFrom(protobuf)) mustBe parent
    }
  }

  property("Respondent must convert to and from protobuf") {
    forAll(institutionRespondentGen) { respondent =>
      val protobuf = respondentToProtobuf(respondent).toByteArray
      respondentFromProtobuf(RespondentMessage.parseFrom(protobuf)) mustBe respondent
    }
  }

  property("Institution must convert to and from protobuf") {
    forAll(institutionGen) { institution =>
      val protobuf = institutionToProtobuf(institution).toByteArray
      institutionFromProtobuf(InstitutionMessage.parseFrom(protobuf)) mustBe institution
    }
  }

}