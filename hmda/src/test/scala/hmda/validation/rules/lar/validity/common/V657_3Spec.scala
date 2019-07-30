package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{NotReverseMortgage, ReverseMortgage}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V657_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V657_3

  property("Rate spread must be NA or Exempt for reverse mortgage") {
    forAll(larGen) { lar =>
      val wrongReverse = lar.copy(reverseMortgage = NotReverseMortgage)
      wrongReverse.mustPass

      val rightReverse = lar.copy(reverseMortgage = ReverseMortgage)
      rightReverse
        .copy(loan = rightReverse.loan.copy(rateSpread = "NA"))
        .mustPass
      rightReverse
        .copy(loan = rightReverse.loan.copy(rateSpread = "Exempt"))
        .mustPass
      rightReverse
        .copy(loan = rightReverse.loan.copy(rateSpread = "test"))
        .mustFail
    }
  }
}
