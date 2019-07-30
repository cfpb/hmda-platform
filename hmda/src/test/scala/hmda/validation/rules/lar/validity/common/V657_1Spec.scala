package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V657_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V657_1

  property("Rate spread must be Valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      lar.copy(loan = lar.loan.copy(rateSpread = "abc")).mustFail
    }
  }
}
