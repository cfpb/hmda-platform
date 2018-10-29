package hmda.serialization.filing.lar

import hmda.model.filing.lar.LarGenerators._
import hmda.persistence.serialization.loanapplicationregister.LoanApplicationRegisterMessage
import hmda.serialization.filing.lar.LoanApplicationRegisterProtobufConverter._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class LoanApplicationRegisterProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("LAR must convert to and from protobuf") {
    forAll(larGen) { lar =>
      val protobuf = loanApplicationRegisterToProtobuf(lar).toByteArray
      loanApplicationRegisterFromProtobuf(
        LoanApplicationRegisterMessage.parseFrom(protobuf)) mustBe lar
    }
  }

}
