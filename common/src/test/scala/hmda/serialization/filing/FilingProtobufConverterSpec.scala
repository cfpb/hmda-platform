package hmda.serialization.filing

import hmda.model.filing.FilingGenerator._
import hmda.persistence.serialization.filing.FilingMessage
import hmda.serialization.filing.FilingProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FilingProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Filing must convert to and from protobuf") {
    forAll(filingGen) { filing =>
      val protobuf = filingToProtobuf(filing).toByteArray
      filingFromProtobuf(FilingMessage.parseFrom(protobuf)) mustBe filing
    }
  }

}