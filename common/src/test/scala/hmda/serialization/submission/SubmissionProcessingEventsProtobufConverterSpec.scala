package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError
}
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import SubmissionProcessingEventsProtobufConverter._
import hmda.persistence.serialization.submission.processing.events.{
  HmdaRowParsedCountMessage,
  HmdaRowParsedErrorMessage
}

class SubmissionProcessingEventsProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("HMDA parsed errors must convert to protobuf and back") {
    forAll { (rowNumber: Int, errors: List[String]) =>
      val parsedError = HmdaRowParsedError(rowNumber, errors)
      val protobuf = hmdaRowParsedErrorToProtobuf(parsedError).toByteArray
      hmdaRowParsedErrorFromProtobuf(
        HmdaRowParsedErrorMessage.parseFrom(protobuf)) mustBe parsedError
    }
  }

  property("HMDA parsed row count must convert to protobuf and back") {
    forAll { i: Int =>
      val parsedCount = HmdaRowParsedCount(i)
      val protobuf = hmdaRowParsedCountToProtobuf(parsedCount).toByteArray
      hmdaRowParsedCountFromProtobuf(
        HmdaRowParsedCountMessage.parseFrom(protobuf)) mustBe parsedCount
    }
  }

}
