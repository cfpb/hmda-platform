package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q629Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q629

  property("Income must be provided for applicable loans") {
    forAll(larGen) { lar =>
      whenever(
        lar.action.actionTakenType == PurchasedLoan ||
          lar.property.totalUnits > 4 ||
          lar.loan.loanPurpose == Refinancing ||
          lar.loan.loanPurpose == CashOutRefinancing) {
        lar.mustPass
      }

      val appLar = lar.copy(action =
                              lar.action.copy(actionTakenType = LoanOriginated),
                            property = lar.property.copy(totalUnits = 3),
                            loan = lar.loan.copy(loanPurpose = HomePurchase))
      appLar.copy(income = "NA").mustFail
      appLar.copy(income = "1").mustPass
    }
  }
}
