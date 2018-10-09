package hmda.serialization.filing

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.FilingGenerator._
import FilingProtobufConverter._
import hmda.persistence.serialization.filing.FilingMessage

class FilingProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Filing must convert to and from protobuf") {
    forAll(filingGen) { filing =>
      val protobuf = filingToProtobuf(filing).toByteArray
      filingFromProtobuf(FilingMessage.parseFrom(protobuf)) mustBe filing
    }
  }

}
