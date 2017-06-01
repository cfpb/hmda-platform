package hmda.persistence.serialization.validation

import hmda.model.fi.lar.LarGenerators
import hmda.model.fi.ts.TsGenerators
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ LarValidated, TsValidated }
import hmda.persistence.model.serialization.CommonHmdaValidator.{ LarValidatedMessage, TsValidatedMessage }
import CommonHmdaValidatorProtobufConverter._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class CommonHmdaValidatorProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators with TsGenerators {

  property("Lar Validated must serialize to protobuf and back") {
    forAll(larGen, submissionIdGen) { (lar, submissionId) =>
      val larValidated = LarValidated(lar, submissionId)
      val protobuf = larValidatedToProtobuf(larValidated).toByteArray
      larValidatedFromProtobuf(LarValidatedMessage.parseFrom(protobuf)) mustBe larValidated
    }
  }

  property("Ts Validated must serialize to protobuf and back") {
    forAll(tsGen) { ts =>
      val tsValidated = TsValidated(ts)
      val protobuf = tsValidatedToProtobuf(tsValidated).toByteArray
      tsValidatedFromProtobuf(TsValidatedMessage.parseFrom(protobuf)) mustBe tsValidated
    }
  }
}
