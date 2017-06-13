package hmda.persistence.serialization.validation

import hmda.model.institution.SubmissionGenerators.submissionIdGen
import hmda.persistence.messages.events.validation.ValidationStatsEvents._
import hmda.persistence.model.MsaGenerators
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class ValidationStatsProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers with MsaGenerators {
  def intGen = Gen.choose(0, 10000)
  def ratioGen = Gen.choose(0d, 1d)

  val serializer = new ValidationStatsProtobufSerializer()

  property("SubmissionSubmittedTotalsAdded must serialize to binary and back") {
    forAll(intGen, submissionIdGen) { (total, subId) =>
      val event = SubmissionSubmittedTotalsAdded(total, subId)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionSubmittedTotalsAddedManifest) mustBe event
    }
  }

  property("SubmissionTaxIdAdded must serialize to binary and back") {
    forAll(Gen.alphaStr, submissionIdGen) { (taxId, subId) =>
      val event = SubmissionTaxIdAdded(taxId, subId)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionTaxIdAddedManifest) mustBe event
    }
  }

  property("SubmissionMacroStatsAdded must serialize to binary and back") {
    forAll(submissionIdGen, ratioGen, ratioGen) { (subId, q075, q076) =>
      val event = SubmissionMacroStatsAdded(subId, intGen.sample.get, intGen.sample.get, intGen.sample.get,
        intGen.sample.get, intGen.sample.get, intGen.sample.get, intGen.sample.get, q075, q076)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionMacroStatsAddedManifest) mustBe event
    }
  }

  property("IrsStatsAdded must serialize to binary and back") {
    forAll(submissionIdGen, listOfMsaGen) { (subId, list) =>
      val event = IrsStatsAdded(list, subId)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.IrsStatsAddedManifest) mustBe event
    }
  }
}
