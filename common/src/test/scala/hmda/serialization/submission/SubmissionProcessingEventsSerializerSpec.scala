package hmda.serialization.submission

import hmda.serialization.submission.HmdaParserErrorStateGenerator._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SubmissionProcessingEventsSerializerSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val serializer = new SubmissionProcessingEventsSerializer

  property("HmdaRowParsedError must serialize to and from binary") {
    forAll(hmdaRowParsedErrorGen) { parsedError =>
      val bytes = serializer.toBinary(parsedError)
      serializer.fromBinary(bytes, serializer.ParsedErrorManifest) mustBe parsedError
    }
  }

  property("HmdaRowParsedCount must serialize to and from binary") {
    forAll(hmdaRowParsedCountGen) { parsedCount =>
      val bytes = serializer.toBinary(parsedCount)
      serializer.fromBinary(bytes, serializer.ParsedErrorCountManifest) mustBe parsedCount
    }
  }

  property("HmdaParserErrorState must serialize to and from binary") {
    forAll(hmdaParserErrorStateGen) { hmdaParserErrorState =>
      val bytes = serializer.toBinary(hmdaParserErrorState)
      serializer.fromBinary(bytes, serializer.HmdaParserErrorStateManifest) mustBe hmdaParserErrorState
    }
  }

}