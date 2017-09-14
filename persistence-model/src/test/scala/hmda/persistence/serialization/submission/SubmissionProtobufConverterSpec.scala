package hmda.persistence.serialization.submission

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.institutions.SubmissionEvents._
import hmda.persistence.model.serialization.SubmissionEvents._
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._
import org.scalacheck.Gen

class SubmissionProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Submission Id must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val protobuf = submissionIdToProtobuf(submissionId).toByteArray
      submissionIdFromProtobuf(SubmissionIdMessage.parseFrom(protobuf)) mustBe submissionId
    }
  }

  property("Submission Status must serialize to protobuf and back") {
    forAll(submissionStatusGen) { submissionStatus =>
      val protobuf = submissionStatusToProtobuf(submissionStatus).toByteArray
      submissionStatusFromProtobuf(SubmissionStatusMessage.parseFrom(protobuf)) mustBe submissionStatus
    }
  }

  property("Submission must serialize to protobuf and back") {
    forAll(submissionGen) { submission =>
      val protobuf = submissionToProtobuf(submission).toByteArray
      submissionFromProtobuf(SubmissionMessage.parseFrom(protobuf)) mustBe submission
    }
  }

  property("Submission Status Updated must serialize to protobuf and back") {
    forAll(submissionIdGen, submissionStatusGen) { (submissionId, submissionStatus) =>
      val submissionStatusUpdated = SubmissionStatusUpdated(submissionId, submissionStatus)
      val protobuf = submissionStatusUpdatedToProtobuf(submissionStatusUpdated).toByteArray
      submissionStatusUpdatedFromProtobuf(SubmissionStatusUpdatedMessage.parseFrom(protobuf)) mustBe submissionStatusUpdated
    }
  }

  property("Submission File Name Added must serialize to protobuf and back") {
    forAll(submissionIdGen, Gen.alphaStr) { (submissionId, fileName) =>
      val submissionFileNameAdded = SubmissionFileNameAdded(submissionId, fileName)
      val protobuf = submissionFileNameAddedToProtobuf(submissionFileNameAdded).toByteArray
      submissionFileNameAddedFromProtobuf(SubmissionFileNameAddedMessage.parseFrom(protobuf)) mustBe submissionFileNameAdded
    }
  }

  property("Submission Created must serialize to protobuf and back") {
    forAll(submissionGen) { submission =>
      val submissionCreated = SubmissionCreated(submission)
      val protobuf = submissionCreatedToProtobuf(submissionCreated).toByteArray
      submissionCreatedFromProtobuf(SubmissionCreatedMessage.parseFrom(protobuf)) mustBe submissionCreated
    }
  }

}
