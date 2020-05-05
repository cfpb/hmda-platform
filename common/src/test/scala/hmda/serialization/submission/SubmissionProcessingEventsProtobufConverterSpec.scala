package hmda.serialization.submission

import hmda.persistence.serialization.submission.processing.events._
import hmda.serialization.submission.HmdaParserErrorStateGenerator._
import hmda.serialization.submission.SubmissionProcessingEventsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SubmissionProcessingEventsProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("HMDA parsed errors must convert to protobuf and back") {
    forAll(hmdaRowParsedErrorGen) { parsedError =>
      val protobuf = hmdaRowParsedErrorToProtobuf(parsedError).toByteArray
      hmdaRowParsedErrorFromProtobuf(HmdaRowParsedErrorMessage.parseFrom(protobuf)) mustBe parsedError
    }
  }

  property("HMDA parsed row count must convert to protobuf and back") {
    forAll(hmdaRowParsedCountGen) { parsedCount =>
      val protobuf = hmdaRowParsedCountToProtobuf(parsedCount).toByteArray
      hmdaRowParsedCountFromProtobuf(HmdaRowParsedCountMessage.parseFrom(protobuf)) mustBe parsedCount
    }
  }

  property("HMDA Parser Error State must convert to protobuf and back") {
    forAll(hmdaParserErrorStateGen) { hmdaParserErrorState =>
      val protobuf =
        hmdaParserErrorStateToProtobuf(hmdaParserErrorState).toByteArray
      hmdaParserErrorStateFromProtobuf(
        HmdaParserErrorStateMessage
          .parseFrom(protobuf)
      ) mustBe hmdaParserErrorState
    }
  }

}