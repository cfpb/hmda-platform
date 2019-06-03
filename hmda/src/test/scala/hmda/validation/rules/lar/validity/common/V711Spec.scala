package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  DebtToIncomeRatio,
  EmptyDenialValue,
  ExemptDenialReason
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V711Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V711

  property("When denial is exempt, all denial values must be valid") {
    forAll(larGen) { lar =>
      whenever(lar.denial.denialReason1 != ExemptDenialReason) {
        lar.mustPass
      }

      val appLar =
        lar.copy(denial = lar.denial.copy(denialReason1 = ExemptDenialReason))
      appLar
        .copy(denial = appLar.denial.copy(denialReason2 = DebtToIncomeRatio))
        .mustFail
      appLar
        .copy(denial = appLar.denial.copy(otherDenialReason = "test"))
        .mustFail

      appLar
        .copy(
          denial = appLar.denial.copy(denialReason2 = EmptyDenialValue,
                                      denialReason3 = EmptyDenialValue,
                                      denialReason4 = EmptyDenialValue,
                                      otherDenialReason = ""))
        .mustPass
    }
  }
}
