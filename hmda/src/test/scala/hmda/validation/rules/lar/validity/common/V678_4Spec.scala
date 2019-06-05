package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.PrimarilyBusinessOrCommercialPurpose
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V678_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V678_4

  property(
    "If loan is for business, prepayment penalty term must be NA or exempt") {
    forAll(larGen) { lar =>
      whenever(
        lar.businessOrCommercialPurpose != PrimarilyBusinessOrCommercialPurpose) {
        lar.mustPass
      }

      val appLar = lar.copy(
        businessOrCommercialPurpose = PrimarilyBusinessOrCommercialPurpose)
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "-10.0"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "10.0"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "NA"))
        .mustPass
    }
  }
}
