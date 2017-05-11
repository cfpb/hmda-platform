package hmda.persistence.serialization.validation

import hmda.model.fi.lar.LarGenerators
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.serialization.HmdaFilingEvents.LarValidatedMessage
import LarValidatedProtobufConverter._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class LarValidatedProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("Lar Validated must serialize to protobuf and back") {
    forAll(larGen, submissionIdGen) { (lar, submissionId) =>
      val larValidated = LarValidated(lar, submissionId)
      val protobuf = larValidatedToProtobuf(larValidated).toByteArray
      larValidatedFromProtobuf(LarValidatedMessage.parseFrom(protobuf)) mustBe larValidated
    }
  }
}
