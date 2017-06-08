package hmda.query.serialization

import hmda.query.view.filing.HmdaFilingView.FilingViewState
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class HmdaFilingViewProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new HmdaFilingViewProtobufSerializer()

  property("FilingViewState must serialize to protobuf and back") {
    forAll(Gen.choose(0: Long, 100000: Long), Gen.choose(0: Long, 100000: Long)) { (size, seq) =>
      val state = FilingViewState(size, seq)
      val bytes = serializer.toBinary(state)
      serializer.fromBinary(bytes, serializer.FilingViewStateManifest) mustBe state
    }
  }
}
