package hmda.serialization.filing

import hmda.messages.filing.FilingEvents.{ FilingCreated, SubmissionAdded, SubmissionUpdated }
import hmda.model.filing.FilingGenerator._
import hmda.model.submission.SubmissionGenerator._
import hmda.persistence.serialization.filing.events.{ FilingCreatedMessage, SubmissionAddedMessage, SubmissionUpdatedMessage }
import hmda.serialization.filing.FilingEventsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FilingEventsProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("FilingCreated must convert to and from protobuf") {
    forAll(filingGen) { filing =>
      val created  = FilingCreated(filing)
      val protobuf = filingCreatedToProtobuf(created).toByteArray
      filingCreatedFromProtobuf(FilingCreatedMessage.parseFrom(protobuf)) mustBe created
    }
  }

  property("SubmissionAdded must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val added    = SubmissionAdded(submission)
      val protobuf = submissionAddedToProtobuf(added).toByteArray
      submissionAddedFromProtobuf(SubmissionAddedMessage.parseFrom(protobuf)) mustBe added
    }
  }

  property("SubmissionUpdated must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val updated  = SubmissionUpdated(submission)
      val protobuf = submissionUpdatedToProtobuf(updated).toByteArray
      submissionUpdatedFromProtoubf(SubmissionUpdatedMessage.parseFrom(protobuf)) mustBe updated
    }
  }

}