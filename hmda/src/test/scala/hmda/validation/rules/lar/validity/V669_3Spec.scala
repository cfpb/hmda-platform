package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V669_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V669_3

  property("Denial reasons can't be duplicates") {
    forAll(larGen) { lar =>
      lar
        .copy(
          denial = lar.denial.copy(denialReason1 = DebtToIncomeRatio,
                                   denialReason2 = CreditHistory,
                                   denialReason3 = DebtToIncomeRatio,
                                   denialReason4 = EmptyDenialValue))
        .mustFail

      lar
        .copy(
          denial = lar.denial.copy(denialReason1 = CreditHistory,
                                   denialReason2 = EmptyDenialValue,
                                   denialReason3 = DebtToIncomeRatio,
                                   denialReason4 = EmptyDenialValue))
        .mustPass
    }
  }
}
