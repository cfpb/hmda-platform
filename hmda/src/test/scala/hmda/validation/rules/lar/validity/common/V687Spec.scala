package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidOtherNonAmortizingFeaturesCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V687Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V687

  property("Other Non-amortizing Features must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      val invalidLar = lar.copy(
        nonAmortizingFeatures = lar.nonAmortizingFeatures.copy(
          otherNonAmortizingFeatures = new InvalidOtherNonAmortizingFeaturesCode))
      invalidLar.mustFail
    }
  }
}
