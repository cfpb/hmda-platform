package hmda.serialization.filing

import hmda.model.filing.FilingDetailsGenerator._
import hmda.persistence.serialization.filing.filingdetails.FilingDetailsMessage
import hmda.serialization.filing.FilingDetailsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FilingDetailsProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Filing Details must convert to and from protobuf") {
    forAll(filingDetailsGen) { filingDetails =>
      val protobuf = filingDetailsToProtobuf(filingDetails).toByteArray
      filingDetailsFromProtobuf(FilingDetailsMessage.parseFrom(protobuf)) mustBe filingDetails
    }
  }

}