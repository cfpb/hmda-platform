package hmda.persistence.serialization.validation

import hmda.persistence.messages.events.validation.SubmissionLarStatsEvents._
import hmda.persistence.model.MsaGenerators
import hmda.persistence.model.serialization.SubmissionLarStatsEvents._
import hmda.persistence.serialization.validation.SubmissionLarStatsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Gen

class SubmissionLarStatsProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers with MsaGenerators {

  property("SubmittedLarsUpdated must convert to protobuf and back") {
    forAll(Gen.choose(0, 100000)) { total =>
      val submittedLarsUpdated = SubmittedLarsUpdated(totalSubmitted = total)
      val protobuf = submittedLarsUpdatedToProtobuf(submittedLarsUpdated).toByteArray
      submittedLarsUpdatedFromProtobuf(SubmittedLarsUpdatedMessage.parseFrom(protobuf)) mustBe submittedLarsUpdated
    }
  }

  property("MacroStatsUpdated must convert to protobuf and back") {
    val intGen = Gen.choose(0, 1000)
    forAll(Gen.choose(0d, 1d)) { d =>
      val macroStatsUpdated = MacroStatsUpdated(
        totalValidated = intGen.sample.get,
        q070Total = intGen.sample.get,
        q070Sold = intGen.sample.get,
        q071Total = intGen.sample.get,
        q071Sold = intGen.sample.get,
        q072Total = intGen.sample.get,
        q072Sold = intGen.sample.get,
        q075Ratio = d,
        q076Ratio = d
      )
      val protobuf = macroStatsUpdatedToProtobuf(macroStatsUpdated).toByteArray
      macroStatsUpdatedFromProtobuf(MacroStatsUpdatedMessage.parseFrom(protobuf)) mustBe macroStatsUpdated
    }
  }

  property("IrsStatsUpdated must convert to protobuf and back") {
    forAll(listOfMsaGen) { list =>
      val irsStatsUpdated = IrsStatsUpdated(list)
      val protobuf = irsStatsUpdatedToProtobuf(irsStatsUpdated).toByteArray
      irsStatsUpdatedFromProtobuf(IrsStatsUpdatedMessage.parseFrom(protobuf)) mustBe irsStatsUpdated
    }
  }
}
