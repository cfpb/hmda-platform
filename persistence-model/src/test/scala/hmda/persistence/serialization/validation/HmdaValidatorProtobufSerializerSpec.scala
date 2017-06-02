package hmda.persistence.serialization.validation

import hmda.model.fi.lar.LarGenerators
import hmda.model.fi.ts.TsGenerators
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ LarValidated, TsValidated }
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class HmdaValidatorProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators with TsGenerators {

  val serializer = new HmdaValidatorProtobufSerializer()

  property("Lar Validated must be serialized to binary and back") {
    forAll(larGen, submissionIdGen) { (lar, submissionId) =>
      val msg = LarValidated(lar, submissionId)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.LarValidatedManifest) mustBe msg
    }
  }

  property("Ts Validated must be serialized to binary and back") {
    forAll(tsGen) { ts =>
      val msg = TsValidated(ts)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.TsValidatedManifest) mustBe msg
    }
  }
}
