package hmda.query.serialization

import hmda.persistence.model.serialization.HmdaFilingViewState.FilingViewStateMessage
import hmda.query.view.filing.HmdaFilingView.FilingViewState
import hmda.query.serialization.HmdaFilingViewProtobufConverter._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class HmdaFilingViewProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {
  property("FilingViewState must serialize to protobuf and back") {
    forAll(Gen.choose(0: Long, 100000: Long), Gen.choose(0: Long, 100000: Long)) { (size, seq) =>
      val state = FilingViewState(size, seq)
      val protobuf = filingViewStateToProtobuf(state).toByteArray
      filingViewStateFromProtobuf(FilingViewStateMessage.parseFrom(protobuf)) mustBe state
    }
  }
}
