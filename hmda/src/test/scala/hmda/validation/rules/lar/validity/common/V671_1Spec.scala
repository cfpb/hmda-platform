package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.OtherDenialReason

class V671_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V671_1

  property(
    "Other Denial Free-form Should not be Blank if Other Denial Selected") {
    forAll(larGen) { lar =>
      whenever(lar.denial.otherDenialReason != "") {
        lar.mustPass
      }

      whenever(
        lar.denial.denialReason1 != OtherDenialReason &&
          lar.denial.denialReason2 != OtherDenialReason &&
          lar.denial.denialReason3 != OtherDenialReason &&
          lar.denial.denialReason4 != OtherDenialReason
      ) {
        lar.mustPass
      }
      val relevantLar1 =
        lar.copy(denial = lar.denial.copy(denialReason1 = OtherDenialReason))
      val relevantLar2 =
        lar.copy(denial = lar.denial.copy(denialReason2 = OtherDenialReason))
      val relevantLar3 =
        lar.copy(denial = lar.denial.copy(denialReason3 = OtherDenialReason))
      val relevantLar4 =
        lar.copy(denial = lar.denial.copy(denialReason4 = OtherDenialReason))

      relevantLar1
        .copy(denial = relevantLar1.denial.copy(otherDenialReason = ""))
        .mustFail
      relevantLar2
        .copy(denial = relevantLar2.denial.copy(otherDenialReason = ""))
        .mustFail
      relevantLar3
        .copy(denial = relevantLar4.denial.copy(otherDenialReason = ""))
        .mustFail
      relevantLar4
        .copy(denial = relevantLar4.denial.copy(otherDenialReason = ""))
        .mustFail
    }
  }
}
