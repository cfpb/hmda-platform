package hmda.serialization.filing

import hmda.model.filing.FilingDetailsGenerator._
import hmda.persistence.serialization.filing.filingdetails.FilingDetailsMessage
import hmda.serialization.filing.FilingDetailsProtobufConverter._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class FilingDetailsProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Filing Details must convert to and from protobuf") {
    forAll(filingDetailsGen) { filingDetails =>
      val protobuf = filingDetailsToProtobuf(filingDetails).toByteArray
      filingDetailsFromProtobuf(FilingDetailsMessage.parseFrom(protobuf)) mustBe filingDetails
    }
  }

}
