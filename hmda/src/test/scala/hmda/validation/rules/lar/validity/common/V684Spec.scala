package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidBalloonPaymentCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V684Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V684

  property("Balloon payment must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      val invalidLar = lar.copy(
        nonAmortizingFeatures = lar.nonAmortizingFeatures.copy(
          balloonPayment = new InvalidBalloonPaymentCode))
      invalidLar.mustFail
    }
  }
}
