package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums._
import hmda.validation.rules.lar.LarEditCheckSpec

class Q654Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q646

  property(
    "An exemption should not be taken") {
    forAll(larGen) { lar =>
      lar.copy(lineOfCredit = ExemptLineOfCredit).mustFail
      lar.copy(nonAmortizingFeatures = lar.nonAmortizingFeatures.copy(balloonPayment = BalloonPaymentExempt)).mustFail
    }
  }

}