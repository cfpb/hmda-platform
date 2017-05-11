package hmda.persistence.serialization.filing

import hmda.model.fi.lar.LarGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated

class HmdaFilingProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  val serializer = new HmdaFilingProtobufSerializer()

  property("Lar Validated must be serialized to binary and back") {
    forAll(larGen, submissionIdGen) { (lar, submissionId) =>
      val msg = LarValidated(lar, submissionId)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.LarValidatedManifest) mustBe msg
    }
  }
}
