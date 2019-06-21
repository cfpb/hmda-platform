package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._2018.LoanApplicationRegister
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
          balloonPayment = InvalidBalloonPaymentCode))
      invalidLar.mustFail
    }
  }
}
