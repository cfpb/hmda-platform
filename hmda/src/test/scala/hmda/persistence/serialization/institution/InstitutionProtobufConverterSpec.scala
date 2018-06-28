package hmda.persistence.serialization.institution

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.InstitutionGenerators._
import InstitutionProtobufConverter._

class InstitutionProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

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
