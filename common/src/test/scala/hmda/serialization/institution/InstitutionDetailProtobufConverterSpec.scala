package hmda.serialization.institution

import hmda.model.institution.InstitutionDetailGenerator._
import hmda.persistence.serialization.institution.institutiondetail.InstitutionDetailMessage
import hmda.serialization.institution.InstitutionDetailProtobufConverter._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class InstitutionDetailProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Filing Details must convert to and from protobuf") {
    forAll(institutionDetailGen) { institutionDetail =>
      val protobuf = institutionDetailToProtobuf(institutionDetail).toByteArray
      institutionDetailFromProtobuf(InstitutionDetailMessage.parseFrom(protobuf)) mustBe institutionDetail
    }
  }

}