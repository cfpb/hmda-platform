package hmda.persistence.serialization.validation

import hmda.model.validation.ValidationErrorGenerators._
import hmda.persistence.model.serialization.HmdaFileValidatorEvents.ValidationErrorMessage
import hmda.persistence.serialization.validation.HmdaFileValidatorProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class HmdaFileValidatorProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("ValidationErrorType must serialize to protobuf and back") {
    forAll(validationErrorTypeGen) { errType =>
      val protobuf = validationErrorTypeToProtobuf(errType)
      validationErrorTypeFromProtobuf(protobuf) mustBe errType
    }
  }

  property("ValidationError must serialize to protobuf and back") {
    forAll(validationErrorGen) { error =>
      val protobuf = validationErrorToProtobuf(error).toByteArray
      validationErrorFromProtobuf(ValidationErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }
}
