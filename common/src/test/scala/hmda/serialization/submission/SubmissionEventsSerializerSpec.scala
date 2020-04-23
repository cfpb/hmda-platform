package hmda.serialization.submission

import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionModified, SubmissionNotExists }
import hmda.model.submission.SubmissionGenerator._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SubmissionEventsSerializerSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val serializer = new SubmissionEventsSerializer()

  property("SubmissionCreated must serialize to and from binary") {
    forAll(submissionGen) { submission =>
      val created      = SubmissionCreated(submission)
      val bytesCreated = serializer.toBinary(created)
      serializer.fromBinary(bytesCreated, serializer.SubmissionCreatedManifest) mustBe created
    }
  }

  property("SubmissionModified must serialize to and from binary") {
    forAll(submissionGen) { submission =>
      val modified      = SubmissionModified(submission)
      val bytesModified = serializer.toBinary(modified)
      serializer.fromBinary(bytesModified, serializer.SubmissionModifiedManifest) mustBe modified
    }
  }

  property("SubmissionNotExists must serialize to and from binary") {
    forAll(submissionGen) { submission =>
      val notExists      = SubmissionNotExists(submission.id)
      val bytesNotExists = serializer.toBinary(notExists)
      serializer.fromBinary(bytesNotExists, serializer.SubmissionNotExistsManifest) mustBe SubmissionNotExists(submission.id)
    }
  }

}