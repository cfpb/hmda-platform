package hmda.persistence.serialization.submission

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionIdMessage
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

class SubmissionProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Submission Id must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val protobuf = submissionIdToProtobuf(submissionId).toByteArray
      submissionIdFromProtobuf(SubmissionIdMessage.parseFrom(protobuf)) mustBe submissionId
    }
  }

}
