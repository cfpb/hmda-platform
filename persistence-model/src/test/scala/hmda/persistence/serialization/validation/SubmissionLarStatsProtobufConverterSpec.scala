package hmda.persistence.serialization.validation

import hmda.persistence.messages.events.validation.SubmissionLarStatsEvents._
import hmda.persistence.model.serialization.SubmissionLarStatsEvents._
import hmda.persistence.serialization.validation.SubmissionLarStatsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Gen

class SubmissionLarStatsProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("SubmittedLarsUpdated must convert to protobuf and back") {
    forAll(Gen.choose(0, 100000)) { total =>
      val submittedLarsUpdated = SubmittedLarsUpdated(totalSubmitted = total)
      val protobuf = submittedLarsUpdatedToProtobuf(submittedLarsUpdated).toByteArray
      submittedLarsUpdatedFromProtobuf(SubmittedLarsUpdatedMessage.parseFrom(protobuf)) mustBe submittedLarsUpdated
    }
  }

  property("MacroStatsUpdated must convert to protobuf and back") {
    val difGen = Gen.choose(-50000, 50000)
    forAll(Gen.choose(0, 50000)) { num =>
      val macroStatsUpdated = MacroStatsUpdated(
        totalValidated = num + difGen.sample.get,
        q070Total = num + difGen.sample.get,
        q070Sold = num + difGen.sample.get,
        q071Total = num + difGen.sample.get,
        q071Sold = num + difGen.sample.get,
        q072Total = num + difGen.sample.get,
        q072Sold = num + difGen.sample.get,
        q075Ratio = num + difGen.sample.get,
        q076Ratio = num + difGen.sample.get
      )
      val protobuf = macroStatsUpdatedToProtobuf(macroStatsUpdated).toByteArray
      macroStatsUpdatedFromProtobuf(MacroStatsUpdatedMessage.parseFrom(protobuf)) mustBe macroStatsUpdated
    }
  }
}
