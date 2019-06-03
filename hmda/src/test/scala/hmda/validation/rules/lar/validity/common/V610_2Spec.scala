package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.{LoanOriginated, PurchasedLoan}

class V610_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V610_2

  property(
    "If Action Taken equals 6, then Application Date must be NA, and the reverse must be true") {
    forAll(larGen) { l =>
      val loan = l.loan.copy(applicationDate = "NA")
      val lar = l.copy(loan = loan,
                       action = l.action.copy(actionTakenType = PurchasedLoan))
      lar.mustPass
    }
  }

  property("Fail if Action Taken is 6 and Application Date is not NA") {
    forAll(larGen) { lar =>
      whenever(lar.loan.applicationDate != "NA") {
        val goodLar =
          lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
        goodLar.mustPass
        val badLar =
          lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))
        badLar.mustFail
      }
    }
  }

}
