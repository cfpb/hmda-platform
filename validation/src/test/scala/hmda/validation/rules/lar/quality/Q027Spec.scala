package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q027Spec extends LarEditCheckSpec {
  property("fails when actionTaken = 1,2,3,4,5,7,8 and propertyType = 1,2 and income NA") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 6 && lar.loan.propertyType != 3) {
        val invalidApplicant = lar.applicant.copy(income = "NA")
        val invalidLar = lar.copy(applicant = invalidApplicant)
        invalidLar.mustFail
      }
    }
  }

  property("passes when applicant income not NA") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.income != "NA") {
        lar.mustPass
      }
    }
  }

  property("passes when action taken not 1,2,3,4,5,7,8") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(actionTakenType = 6)
      validLar.mustPass
    }
  }

  property("passes when property type not 1, 2") {
    forAll(larGen) { lar =>
      val validLoan = lar.loan.copy(propertyType = 3)
      val validLar = lar.copy(loan = validLoan)
      validLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q027
}
