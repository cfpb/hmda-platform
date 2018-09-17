package hmda.serialization.submission

import hmda.persistence.serialization.submission.{
  SubmissionIdMessage,
  SubmissionMessage
}
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.submission.SubmissionGenerator._
import SubmissionProtobufConverter._

class SubmissionProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Submission Id must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val protobuf = submissionIdToProtobuf(submissionId)
        .getOrElse(SubmissionIdMessage())
        .toByteArray
      submissionIdFromProtobuf(SubmissionIdMessage.parseFrom(protobuf)) mustBe submissionId
    }
  }

  property("Submission must serialize to protobuf and back") {
    forAll(submissionGen) { submission =>
      val protobuf = submissionToProtobuf(submission).toByteArray
      submissionFromProtobuf(SubmissionMessage.parseFrom(protobuf)) mustBe submission
    }
  }

}
