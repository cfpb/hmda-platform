package hmda.serialization.filing

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.FilingDetailsGenerator._
import FilingDetailsProtobufConverter._
import hmda.persistence.serialization.filing.filingdetails.FilingDetailsMessage

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
