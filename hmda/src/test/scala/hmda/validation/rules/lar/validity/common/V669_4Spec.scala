package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V669_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V669_4

  property("If denial reason 1 is exempt or NA, then 2-4 must be blank") {
    forAll(larGen) { lar =>
      whenever(lar.denial.denialReason1 != DenialReasonNotApplicable && lar.denial.denialReason1 != ExemptDenialReason) {
        lar.mustPass
      }

      val appLar =
        lar.copy(denial = lar.denial.copy(denialReason1 = ExemptDenialReason))
      appLar
        .copy(
          denial = appLar.denial.copy(denialReason2 = EmptyDenialValue,
                                      denialReason3 = EmptyDenialValue,
                                      denialReason4 = EmptyDenialValue))
        .mustPass

      appLar
        .copy(
          denial = appLar.denial.copy(denialReason2 = ExemptDenialReason,
                                      denialReason3 = ExemptDenialReason,
                                      denialReason4 = EmptyDenialValue))
        .mustFail
    }
  }
}
