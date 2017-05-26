package hmda.persistence.serialization.validation

import hmda.model.validation.ValidationErrorGenerators._
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.persistence.model.serialization.HmdaFileValidatorEvents._
import hmda.persistence.serialization.validation.HmdaFileValidatorProtobufConverter._
import org.scalacheck.Gen
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

  property("EditsVerified must serialize to protobuf and back") {
    forAll(validationErrorTypeGen, Gen.oneOf(true, false)) { (errType, verified) =>
      val verifiedEvent = EditsVerified(errType, verified)
      val protobuf = editsVerifiedToProtobuf(verifiedEvent).toByteArray
      editsVerifiedFromProtobuf(EditsVerifiedMessage.parseFrom(protobuf)) mustBe verifiedEvent
    }
  }

  property("TsSyntacticalError must serialize to protobuf and back") {
    forAll(syntacticalValidationErrorGen) { innerError =>
      val error = TsSyntacticalError(innerError)
      val protobuf = tsSyntacticalErrorToProtobuf(error).toByteArray
      tsSyntacticalErrorFromProtobuf(TsSyntacticalErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }
  property("TsValidityError must serialize to protobuf and back") {
    forAll(validityValidationErrorGen) { innerError =>
      val error = TsValidityError(innerError)
      val protobuf = tsValidityErrorToProtobuf(error).toByteArray
      tsValidityErrorFromProtobuf(TsValidityErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }
  property("TsQualityError must serialize to protobuf and back") {
    forAll(qualityValidationErrorGen) { innerError =>
      val error = TsQualityError(innerError)
      val protobuf = tsQualityErrorToProtobuf(error).toByteArray
      tsQualityErrorFromProtobuf(TsQualityErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }

  property("LarSyntacticalError must serialize to protobuf and back") {
    forAll(syntacticalValidationErrorGen) { innerError =>
      val error = LarSyntacticalError(innerError)
      val protobuf = larSyntacticalErrorToProtobuf(error).toByteArray
      larSyntacticalErrorFromProtobuf(LarSyntacticalErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }
  property("LarValidityError must serialize to protobuf and back") {
    forAll(validityValidationErrorGen) { innerError =>
      val error = LarValidityError(innerError)
      val protobuf = larValidityErrorToProtobuf(error).toByteArray
      larValidityErrorFromProtobuf(LarValidityErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }
  property("LarQualityError must serialize to protobuf and back") {
    forAll(qualityValidationErrorGen) { innerError =>
      val error = LarQualityError(innerError)
      val protobuf = larQualityErrorToProtobuf(error).toByteArray
      larQualityErrorFromProtobuf(LarQualityErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }
  property("LarMacroError must serialize to protobuf and back") {
    forAll(macroValidationErrorGen) { innerError =>
      val error = LarMacroError(innerError)
      val protobuf = larMacroErrorToProtobuf(error).toByteArray
      larMacroErrorFromProtobuf(LarMacroErrorMessage.parseFrom(protobuf)) mustBe error
    }
  }
}
