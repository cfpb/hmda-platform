package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidNegativeArmotizationCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V686Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V686

  property("Negative Amortization must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      val invalidLar = lar.copy(
        nonAmortizingFeatures = lar.nonAmortizingFeatures.copy(
          negativeAmortization = new InvalidNegativeArmotizationCode))
      invalidLar.mustFail
    }
  }
}
