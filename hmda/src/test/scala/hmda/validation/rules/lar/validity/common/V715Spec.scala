package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.{LoanApplicationRegister, NonAmortizingFeatures}
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V715Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V715

  property(
    "If Non-Amortizing Features exemption election is taken, all fields must be exempt") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(nonAmortizingFeatures = NonAmortizingFeatures())
      unappLar.mustPass

      val oneExempt = lar.copy(
        nonAmortizingFeatures =
          lar.nonAmortizingFeatures.copy(balloonPayment = BalloonPaymentExempt))
      oneExempt
        .copy(
          nonAmortizingFeatures = oneExempt.nonAmortizingFeatures.copy(
            interestOnlyPayments = InterestOnlyPayment))
        .mustFail

      val allExempt = oneExempt.copy(
        nonAmortizingFeatures = oneExempt.nonAmortizingFeatures.copy(
          interestOnlyPayments = InterestOnlyPaymentExempt,
          otherNonAmortizingFeatures = OtherNonAmortizingFeaturesExempt,
          negativeAmortization = NegativeAmortizationExempt
        ))
      allExempt.mustPass
    }
  }
}
