package hmda.serialization.filing

import hmda.messages.filing.FilingEvents.{
  FilingCreated,
  FilingStatusUpdated,
  SubmissionAdded,
  SubmissionUpdated
}
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.FilingGenerator._
import hmda.model.submission.SubmissionGenerator._
import FilingEventsProtobufConverter._
import hmda.persistence.serialization.filing.events.{
  FilingCreatedMessage,
  FilingStatusUpdatedMessage,
  SubmissionAddedMessage,
  SubmissionUpdatedMessage
}

class FilingEventsProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("FilingCreated must convert to and from protobuf") {
    forAll(filingGen) { filing =>
      val created = FilingCreated(filing)
      val protobuf = filingCreatedToProtobuf(created).toByteArray
      filingCreatedFromProtobuf(FilingCreatedMessage.parseFrom(protobuf)) mustBe created
    }
  }

  property("FilingStatusUpdated must convert to and from protobuf") {
    forAll(filingGen) { filing =>
      val updated = FilingStatusUpdated(filing)
      val protobuf = filingStatusUpdatedToProtobuf(updated).toByteArray
      filingStatusUpdatedFromProtobuf(
        FilingStatusUpdatedMessage
          .parseFrom(protobuf)) mustBe updated
    }
  }

  property("SubmissionAdded must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val added = SubmissionAdded(submission)
      val protobuf = submissionAddedToProtobuf(added).toByteArray
      submissionAddedFromProtobuf(SubmissionAddedMessage.parseFrom(protobuf)) mustBe added
    }
  }

  property("SubmissionUpdated must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val updated = SubmissionUpdated(submission)
      val protobuf = submissionUpdatedToProtobuf(updated).toByteArray
      submissionUpdatedFromProtoubf(
        SubmissionUpdatedMessage.parseFrom(protobuf)) mustBe updated
    }
  }

}
