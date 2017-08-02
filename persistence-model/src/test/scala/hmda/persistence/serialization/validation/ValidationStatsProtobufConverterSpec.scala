package hmda.persistence.serialization.validation

import hmda.model.institution.SubmissionGenerators.submissionIdGen
import hmda.persistence.messages.events.validation.ValidationStatsEvents._
import hmda.persistence.model.MsaGenerators
import hmda.persistence.model.serialization.ValidationStatsEvents._
import hmda.persistence.serialization.validation.ValidationStatsProtobufConverter._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class ValidationStatsProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers with MsaGenerators {
  def intGen = Gen.choose(0, 10000)
  def ratioGen = Gen.choose(0d, 1d)

  property("SubmissionSubmittedTotalsAdded must convert to Protobuf and back") {
    forAll(intGen, submissionIdGen) { (total, subId) =>
      val event = SubmissionSubmittedTotalsAdded(total, subId)
      val protobuf = submissionSubmittedTotalsAddedToProtobuf(event).toByteArray
      submissionSubmittedTotalsAddedFromProtobuf(SubmissionSubmittedTotalsAddedMessage.parseFrom(protobuf)) mustBe event
    }
  }

  property("SubmissionTaxIdAdded must convert to Protobuf and back") {
    forAll(Gen.alphaStr, submissionIdGen) { (taxId, subId) =>
      val event = SubmissionTaxIdAdded(taxId, subId)
      val protobuf = submissionTaxIdAddedToProtobuf(event).toByteArray
      submissionTaxIdAddedFromProtobuf(SubmissionTaxIdAddedMessage.parseFrom(protobuf)) mustBe event
    }
  }

  property("SubmissionMacroStatsAdded must convert to Protobuf and back") {
    forAll(submissionIdGen, ratioGen, ratioGen) { (subId, q075, q076) =>
      val event = SubmissionMacroStatsAdded(subId, intGen.sample.get, intGen.sample.get, intGen.sample.get,
        intGen.sample.get, intGen.sample.get, intGen.sample.get, intGen.sample.get, q075, q076)
      val protobuf = submissionMacroStatsAddedToProtobuf(event).toByteArray
      submissionMacroStatsAddedFromProtobuf(SubmissionMacroStatsAddedMessage.parseFrom(protobuf)) mustBe event
    }
  }

  property("IrsStatsAdded must convert to Protobuf and back") {
    forAll(submissionIdGen, listOfMsaGen) { (subId, list) =>
      val event = IrsStatsAdded(list, subId)
      val protobuf = irsStatsAddedToProtobuf(event).toByteArray
      irsStatsAddedFromProtobuf(IrsStatsAddedMessage.parseFrom(protobuf)) mustBe event
    }
  }

}
