package hmda.validation.rules.lar.validity

import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._2018.LoanApplicationRegister

class V610_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V610_1

  property(
    "Application Date must be valid YYYMMDD format or NA, and cannot be left blank") {
    forAll(larGen) { lar =>
      lar.mustPass
      lar.copy(loan = lar.loan.copy(applicationDate = "2016121")).mustFail
    }
  }
}
