package hmda.serialization.submission

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import HmdaParserErrorStateGenerator._

class SubmissionProcessingEventsSerializerSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

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
