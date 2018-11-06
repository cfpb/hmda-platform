package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError
}
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks

class SubmissionProcessingEventsSerializerSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  val serializer = new SubmissionProcessingEventsSerializer

  property("HmdaRowParsedError must serialize to and from binary") {
    forAll { (rowNumber: Int, errors: List[String]) =>
      val parsedError = HmdaRowParsedError(rowNumber, errors)
      val bytes = serializer.toBinary(parsedError)
      serializer.fromBinary(bytes, serializer.ParsedErrorManifest) mustBe parsedError
    }
  }

  property("HmdaRowParsedCount must serialize to and from binary") {
    forAll { i: Int =>
      val parsedCount = HmdaRowParsedCount(i)
      val bytes = serializer.toBinary(parsedCount)
      serializer.fromBinary(bytes, serializer.ParsedErrorCountManifest) mustBe parsedCount
    }
  }

}
