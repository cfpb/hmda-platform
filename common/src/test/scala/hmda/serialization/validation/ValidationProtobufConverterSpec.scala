package hmda.serialization.validation

import ValidationErrorGenerator._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import ValidationProtobufConverter._
import hmda.persistence.serialization.validation.ValidationErrorMessage

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
