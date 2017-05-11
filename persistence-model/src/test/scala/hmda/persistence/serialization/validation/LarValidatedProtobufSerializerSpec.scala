package hmda.persistence.serialization.validation

import hmda.model.fi.lar.LarGenerators
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class LarValidatedProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  val serializer = new LarValidatedProtobufSerializer()

  property("Lar Validated must be serialized to binary and back") {
    forAll(larGen, submissionIdGen) { (lar, submissionId) =>
      val msg = LarValidated(lar, submissionId)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.LarValidatedManifest) mustBe msg
    }
  }
}
