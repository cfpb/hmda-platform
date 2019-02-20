package hmda.serialization.validation

import hmda.persistence.serialization.validation.ValidationErrorMessage
import hmda.serialization.validation.ValidationErrorGenerator._
import hmda.serialization.validation.ValidationProtobufConverter._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class ValidationProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Validation Error must serialize to protobuf and back") {
    forAll(validationErrorGen) { validationError =>
      val protobuf = validationErrorToProtobuf(validationError).toByteArray
      validationErrorFromProtobuf(ValidationErrorMessage.parseFrom(protobuf)) mustBe validationError
    }
  }

}
