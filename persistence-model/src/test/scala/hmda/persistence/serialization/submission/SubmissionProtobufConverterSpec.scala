package hmda.persistence.serialization.submission

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.model.serialization.SubmissionEvents.{ SubmissionIdMessage, SubmissionMessage, SubmissionStatusMessage }
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

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

}
