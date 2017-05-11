package hmda.persistence.serialization.filing

import hmda.model.fi.lar.LarGenerators
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.persistence.serialization.filing.HmdaFilingProtobufConverter._
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.model.serialization.HmdaFilingEvents.LarValidatedMessage

class HmdaFilingProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("Lar Validated must serialize to protobuf and back") {
    forAll(larGen, submissionIdGen) { (lar, submissionId) =>
      val larValidated = LarValidated(lar, submissionId)
      val protobuf = larValidatedToProtobuf(larValidated).toByteArray
      larValidatedFromProtobuf(LarValidatedMessage.parseFrom(protobuf)) mustBe larValidated
    }
  }
}
