package hmda.persistence.serialization.submission

import hmda.model.institution.SubmissionGenerators.submissionGen
import hmda.persistence.messages.events.processing.SubmissionFSMEvents._
import hmda.persistence.model.serialization.SubmissionFSM._
import hmda.persistence.serialization.submission.SubmissionFSMProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class SubmissionFSMProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {
  property("SubmissionFSMCreated must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionFSMCreated(s)
      val protobuf = submissionFSMCreatedToProtobuf(event).toByteArray
      submissionFSMCreatedFromProtobuf(SubmissionFSMCreatedMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionUploading must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionUploading(s)
      val protobuf = submissionUploadingToProtobuf(event).toByteArray
      submissionUploadingFromProtobuf(SubmissionUploadingMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionUploaded must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionUploaded(s)
      val protobuf = submissionUploadedToProtobuf(event).toByteArray
      submissionUploadedFromProtobuf(SubmissionUploadedMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionParsing must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionParsing(s)
      val protobuf = submissionParsingToProtobuf(event).toByteArray
      submissionParsingFromProtobuf(SubmissionParsingMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionParsed must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionParsed(s)
      val protobuf = submissionParsedToProtobuf(event).toByteArray
      submissionParsedFromProtobuf(SubmissionParsedMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionParsedWithErrors must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionParsedWithErrors(s)
      val protobuf = submissionParsedWithErrorsToProtobuf(event).toByteArray
      submissionParsedWithErrorsFromProtobuf(SubmissionParsedWithErrorsMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionValidating must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionValidating(s)
      val protobuf = submissionValidatingToProtobuf(event).toByteArray
      submissionValidatingFromProtobuf(SubmissionValidatingMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionValidated must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionValidated(s)
      val protobuf = submissionValidatedToProtobuf(event).toByteArray
      submissionValidatedFromProtobuf(SubmissionValidatedMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionValidatedWithErrors must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionValidatedWithErrors(s)
      val protobuf = submissionValidatedWithErrorsToProtobuf(event).toByteArray
      submissionValidatedWithErrorsFromProtobuf(SubmissionValidatedWithErrorsMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionSigned must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionSigned(s)
      val protobuf = submissionSignedToProtobuf(event).toByteArray
      submissionSignedFromProtobuf(SubmissionSignedMessage.parseFrom(protobuf)) mustBe event
    }
  }
  property("SubmissionFailed must convert to protobuf and back") {
    forAll(submissionGen) { s =>
      val event = SubmissionFailed(s)
      val protobuf = submissionFailedToProtobuf(event).toByteArray
      submissionFailedFromProtobuf(SubmissionFailedMessage.parseFrom(protobuf)) mustBe event
    }
  }

}
