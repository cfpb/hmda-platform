package hmda.persistence.serialization.validation

import hmda.model.validation.ValidationErrorGenerators._
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class HmdaFileValidatorProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {

  val serializer = new HmdaFileValidatorProtobufSerializer

  property("EditsVerified must serialize to binary and back") {
    forAll(validationErrorTypeGen, Gen.oneOf(true, false)) { (errType, verified) =>
      val verifiedEvent = EditsVerified(errType, verified)
      val bytes = serializer.toBinary(verifiedEvent)
      serializer.fromBinary(bytes, serializer.EditsVerifiedManifest) mustBe verifiedEvent
    }
  }

  property("TsSyntacticalError must serialize to binary and back") {
    forAll(syntacticalValidationErrorGen) { innerError =>
      val error = TsSyntacticalError(innerError)
      val bytes = serializer.toBinary(error)
      serializer.fromBinary(bytes, serializer.TsSyntacticalErrorManifest) mustBe error
    }
  }
  property("TsValidityError must serialize to binary and back") {
    forAll(validityValidationErrorGen) { innerError =>
      val error = TsValidityError(innerError)
      val bytes = serializer.toBinary(error)
      serializer.fromBinary(bytes, serializer.TsValidityErrorManifest) mustBe error
    }
  }
  property("TsQualityError must serialize to binary and back") {
    forAll(qualityValidationErrorGen) { innerError =>
      val error = TsQualityError(innerError)
      val bytes = serializer.toBinary(error)
      serializer.fromBinary(bytes, serializer.TsQualityErrorManifest) mustBe error
    }
  }

  property("LarSyntacticalError must serialize to binary and back") {
    forAll(syntacticalValidationErrorGen) { innerError =>
      val error = LarSyntacticalError(innerError)
      val bytes = serializer.toBinary(error)
      serializer.fromBinary(bytes, serializer.LarSyntacticalErrorManifest) mustBe error
    }
  }
  property("LarValidityError must serialize to binary and back") {
    forAll(validityValidationErrorGen) { innerError =>
      val error = LarValidityError(innerError)
      val bytes = serializer.toBinary(error)
      serializer.fromBinary(bytes, serializer.LarValidityErrorManifest) mustBe error
    }
  }
  property("LarQualityError must serialize to binary and back") {
    forAll(qualityValidationErrorGen) { innerError =>
      val error = LarQualityError(innerError)
      val bytes = serializer.toBinary(error)
      serializer.fromBinary(bytes, serializer.LarQualityErrorManifest) mustBe error
    }
  }
  property("LarMacroError must serialize to binary and back") {
    forAll(macroValidationErrorGen) { innerError =>
      val error = LarMacroError(innerError)
      val bytes = serializer.toBinary(error)
      serializer.fromBinary(bytes, serializer.LarMacroErrorManifest) mustBe error
    }
  }
}
