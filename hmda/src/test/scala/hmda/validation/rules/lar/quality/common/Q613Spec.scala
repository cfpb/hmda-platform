package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q613Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q613

  property("If primarly business, loan purpose should be applicable") {
    forAll(larGen) { lar =>
      whenever(
        lar.businessOrCommercialPurpose != PrimarilyBusinessOrCommercialPurpose) {
        lar.mustPass
      }

      val appLar = lar.copy(
        businessOrCommercialPurpose = PrimarilyBusinessOrCommercialPurpose)
      appLar.copy(loan = appLar.loan.copy(loanPurpose = HomePurchase)).mustPass
      appLar
        .copy(loan = appLar.loan.copy(loanPurpose = new InvalidLoanPurposeCode))
        .mustFail
    }
  }
}
