package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.PurchasedLoan
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q648Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q648

  property(
    "If loan is not purchased first characters of ULI should match LEI") {
    val lei = "abcdefghijklmnopqrstuvwxyz"
    val validUli = "abcdefghijklmnopqrst"
    val shortUli = "abcdefghijklmnopq"
    val invalidUli = "abcdefghijklmnopqrszasas"
    
    forAll(larGen) { lar =>
      whenever(
        lar.action.actionTakenType != PurchasedLoan) {
        val relevantLar = lar.copy(larIdentifier = lar.larIdentifier.copy(LEI = lei))
        val validLar = relevantLar.copy(loan = relevantLar.loan.copy(ULI = validUli))
        val shortLar = relevantLar.copy(loan = relevantLar.loan.copy(ULI = shortUli))
        val invalidLar = relevantLar.copy(loan = relevantLar.loan.copy(ULI = invalidUli))
        validLar.mustPass
        shortLar.mustPass
        invalidLar.mustFail
      }
      val purchasedLoan =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan),loan = lar.loan.copy(ULI = validUli),larIdentifier = lar.larIdentifier.copy(LEI = lei))
      purchasedLoan.mustPass

    }
  }
}
