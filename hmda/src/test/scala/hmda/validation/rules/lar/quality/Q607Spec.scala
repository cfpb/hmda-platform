package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.SecuredBySubordinateLien

class Q607Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q607

  property("Subordinate Lien Loans should be less than 250000") {
    forAll(larGen) { lar =>
      val relevantLar = lar.copy(lienStatus = SecuredBySubordinateLien)
      whenever(lar.lienStatus != SecuredBySubordinateLien) {
        lar.mustPass
      }
      whenever(lar.loan.amount >= 250000.0) {
        lar.copy(lienStatus = SecuredBySubordinateLien).mustFail
      }
      relevantLar.copy(loan = lar.loan.copy(amount = 250000.0)).mustFail
      relevantLar.copy(loan = lar.loan.copy(amount = 249999.0)).mustPass
    }
  }
}
