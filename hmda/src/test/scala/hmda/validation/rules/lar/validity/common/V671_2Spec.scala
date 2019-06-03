package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.{EmptyDenialValue, OtherDenialReason}

class V671_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V671_2

  property(
    "If Other Denial Free-form is Blank, Other Denial Should be Selected") {
    forAll(larGen) { lar =>
      lar
        .copy(denial = lar.denial.copy(otherDenialReason = ""))
        .mustPass

      lar
        .copy(denial = lar.denial.copy(denialReason1 = OtherDenialReason))
        .mustPass

      lar
        .copy(denial = lar.denial.copy(denialReason2 = OtherDenialReason))
        .mustPass

      lar
        .copy(denial = lar.denial.copy(denialReason3 = OtherDenialReason))
        .mustPass

      lar
        .copy(denial = lar.denial.copy(denialReason4 = OtherDenialReason))
        .mustPass

      whenever(lar.denial.otherDenialReason != "") {
        lar
          .copy(
            denial = lar.denial.copy(
              denialReason1 = EmptyDenialValue,
              denialReason2 = EmptyDenialValue,
              denialReason3 = EmptyDenialValue,
              denialReason4 = EmptyDenialValue
            ))
          .mustFail
      }
    }
  }
}
