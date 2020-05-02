package hmda.serialization.filing

import hmda.messages.filing.FilingEvents.{ FilingCreated, FilingStatusUpdated, SubmissionAdded, SubmissionUpdated }
import hmda.model.filing.FilingGenerator._
import hmda.model.submission.SubmissionGenerator._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FilingEventsSerializerSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val serializer = new FilingEventsSerializer()

  property("FilingCreated must serialize to and from binary") {
    forAll(filingGen) { filing =>
      val created      = FilingCreated(filing)
      val bytesCreated = serializer.toBinary(created)
      serializer.fromBinary(bytesCreated, serializer.FilingCreatedManifest) mustBe created
    }
  }

  property("FilingStatusUpdated must serialize to and from binary") {
    forAll(filingGen) { filing =>
      val updated      = FilingStatusUpdated(filing)
      val bytesUpdated = serializer.toBinary(updated)
      serializer.fromBinary(bytesUpdated, serializer.FilingStatusUpdatedManifest) mustBe updated
    }
  }

  property("SubmissionAdded must serialize to and from binary") {
    forAll(submissionGen) { submission =>
      val added      = SubmissionAdded(submission)
      val bytesAdded = serializer.toBinary(added)
      serializer.fromBinary(
        bytesAdded,
        serializer.SubmissionAddedManifest
      ) mustBe added
    }
  }

  property("SubmissionUpdated must serialize to and from binary") {
    forAll(submissionGen) { submission =>
      val updated      = SubmissionUpdated(submission)
      val bytesUpdated = serializer.toBinary(updated)
      serializer.fromBinary(
        bytesUpdated,
        serializer.SubmissionUpdatedManifest
      ) mustBe updated
    }
  }

}