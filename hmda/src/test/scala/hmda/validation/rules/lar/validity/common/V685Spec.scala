package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidInterestOnlyPaymentCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V685Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V685

  property("Total units must be greater than 0") {
    forAll(larGen) { lar =>
      lar.mustPass

      val invalidLar = lar.copy(
        nonAmortizingFeatures = lar.nonAmortizingFeatures.copy(
          interestOnlyPayments = new InvalidInterestOnlyPaymentCode))
      invalidLar.mustFail
    }
  }
}
