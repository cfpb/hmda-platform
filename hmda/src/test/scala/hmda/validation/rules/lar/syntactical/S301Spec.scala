package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.lar.LarGenerators._
import hmda.validation.context.ValidationContext
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class S301Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] =
    S301.withContext(
      ValidationContext(ts = Some(TransmittalSheet(LEI = "test"))))

  property("Pass when LEI is reported correctly") {
    forAll(larGen) { lar =>
      val validLar =
        lar.copy(larIdentifier = lar.larIdentifier.copy(LEI = "test"))
      validLar.mustPass
    }
  }

  property("Pass when LEI is reported correctly in a different case") {
    forAll(larGen) { lar =>
      val validLar =
        lar.copy(larIdentifier = lar.larIdentifier.copy(LEI = "TEST"))
      validLar.mustPass
    }
  }

  property("Fail when LEI is reported incorrectly") {
    forAll(larGen) { lar =>
      whenever(lar.larIdentifier.LEI != "test") {
        lar.mustFail
      }
    }
  }

}
