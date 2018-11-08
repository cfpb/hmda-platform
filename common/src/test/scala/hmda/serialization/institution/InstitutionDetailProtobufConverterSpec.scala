package hmda.serialization.institution

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.InstitutionDetailGenerator._
import InstitutionDetailProtobufConverter._
import hmda.persistence.serialization.institution.institutiondetail.InstitutionDetailMessage

class InstitutionDetailProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Filing Details must convert to and from protobuf") {
    forAll(institutionDetailGen) { institutionDetail =>
      val protobuf = institutionDetailToProtobuf(institutionDetail).toByteArray
      institutionDetailFromProtobuf(
        InstitutionDetailMessage.parseFrom(protobuf)) mustBe institutionDetail
    }
  }

}
