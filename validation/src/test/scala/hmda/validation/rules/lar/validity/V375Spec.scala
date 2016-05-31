package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V375Spec extends LarEditCheckSpec {

  property("if purchaser type is 2, then loan type 2, 3, or 4 must succeed") {
    forAll(larGen) { lar =>
      val newLar = lar.copy(purchaserType = 2)
      whenever(List(2, 3, 4).contains(newLar.loan.loanType)) {
        newLar.mustPass
      }
    }
  }

  property("if purchaser type is 2, then loan type other than 2, 3, or 4 must fail") {
    forAll(larGen) { lar =>
      val newLoan = lar.loan.copy(loanType = 1)
      val newLar = lar.copy(purchaserType = 2, loan = newLoan)
      newLar.mustFail
    }
  }

  property("if purchaser type is not 2, then any loan type succeeds") {
    forAll(larGen) { lar =>
      whenever(lar.purchaserType != 2) {
        lar.mustPass
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V375
}
