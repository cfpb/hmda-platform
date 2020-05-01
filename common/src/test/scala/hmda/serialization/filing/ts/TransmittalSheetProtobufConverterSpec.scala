package hmda.serialization.filing.ts

import hmda.model.filing.ts.TsGenerators._
import hmda.persistence.serialization.transmittalsheet._
import hmda.serialization.filing.ts.TransmittalSheetProtobufConverter._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class TransmittalSheetProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("TS must convert to and from protobuf") {
    forAll(tsGen) { ts =>
      val protobuf = transmittalSheetToProtobuf(ts).toByteArray
      transmittalSheetFromProtobuf(TransmittalSheetMessage.parseFrom(protobuf)) mustBe ts
    }
  }

}