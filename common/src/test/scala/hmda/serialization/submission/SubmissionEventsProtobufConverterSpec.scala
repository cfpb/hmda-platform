package hmda.serialization.submission

import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionModified, SubmissionNotExists }
import hmda.model.submission.SubmissionGenerator._
import hmda.persistence.serialization.submission.events.{ SubmissionCreatedMessage, SubmissionModifiedMessage, SubmissionNotExistsMessage }
import hmda.serialization.submission.SubmissionEventsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SubmissionEventsProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("SubmissionCreated must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val created  = SubmissionCreated(submission)
      val protobuf = submissionCreatedToProtobuf(created).toByteArray
      submissionCreatedFromProtobuf(SubmissionCreatedMessage.parseFrom(protobuf)) mustBe created
    }
  }

  property("SubmissionModified must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val modified = SubmissionModified(submission)
      val protobuf = submissionModifiedToProtobuf(modified).toByteArray
      submissionModifiedFromProtobuf(SubmissionModifiedMessage.parseFrom(protobuf)) mustBe modified
    }
  }

  property("SubmissionNotExists must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val protobuf = submissionNotExistsToProtobuf(SubmissionNotExists(submission.id)).toByteArray
      submissionNotExistsFromProtobuf(
        SubmissionNotExistsMessage
          .parseFrom(protobuf)
      ) mustBe SubmissionNotExists(submission.id)
    }
  }

}