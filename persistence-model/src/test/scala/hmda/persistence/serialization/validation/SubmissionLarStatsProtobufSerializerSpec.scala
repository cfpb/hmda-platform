package hmda.persistence.serialization.validation

import hmda.persistence.messages.events.validation.SubmissionLarStatsEvents.{ MacroStatsUpdated, SubmittedLarsUpdated }
import hmda.persistence.model.serialization.SubmissionLarStatsEvents.{ MacroStatsUpdatedMessage, SubmittedLarsUpdatedMessage }
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class SubmissionLarStatsProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new SubmissionLarStatsProtobufSerializer()

  property("SubmittedLarsUpdated messages must be serialized to binary and back") {
    forAll(Gen.choose(0, 100000)) { total =>
      val msg = SubmittedLarsUpdated(totalSubmitted = total)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.SubmittedLarsUpdatedManifest) mustBe msg
    }
  }

  property("MacroStatsUpdated messages must be serialized to binary and back") {
    val difGen = Gen.choose(-50000, 50000)
    forAll(Gen.choose(0, 50000)) { num =>
      val msg = MacroStatsUpdated(
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
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.MacroStatsUpdatedManifest) mustBe msg
    }
  }
}
