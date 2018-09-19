package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V683Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V683

  property("Total units must be greater than 0") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar.copy(loan = lar.loan.copy(introductoryRatePeriod = "test")).mustFail
      lar.copy(loan = lar.loan.copy(introductoryRatePeriod = "-100")).mustFail
    }
  }
}
