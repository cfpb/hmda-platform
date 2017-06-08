package hmda.persistence.serialization.submission

import hmda.model.institution.SubmissionGenerators.submissionGen
import hmda.persistence.messages.events.processing.SubmissionFSMEvents._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class SubmissionFSMProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new SubmissionFSMProtobufSerializer()

  property("SubmissionFSMCreated must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionFSMCreated(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionFSMCreatedManifest) mustBe event
    }
  }
  property("SubmissionUploading must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionUploading(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionUploadingManifest) mustBe event
    }
  }
  property("SubmissionUploaded must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionUploaded(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionUploadedManifest) mustBe event
    }
  }
  property("SubmissionParsing must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionParsing(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionParsingManifest) mustBe event
    }
  }
  property("SubmissionParsed must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionParsed(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionParsedManifest) mustBe event
    }
  }
  property("SubmissionParsedWithErrors must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionParsedWithErrors(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionParsedWithErrorsManifest) mustBe event
    }
  }
  property("SubmissionValidating must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionValidating(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionValidatingManifest) mustBe event
    }
  }
  property("SubmissionValidated must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionValidated(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionValidatedManifest) mustBe event
    }
  }
  property("SubmissionValidatedWithErrors must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionValidatedWithErrors(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionValidatedWithErrorsManifest) mustBe event
    }
  }
  property("SubmissionSigned must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionSigned(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionSignedManifest) mustBe event
    }
  }
  property("SubmissionFailed must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionFailed(s)
      val bytes = serializer.toBinary(event)
      serializer.fromBinary(bytes, serializer.SubmissionFailedManifest) mustBe event
    }
  }

}
