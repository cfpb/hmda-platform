package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{
  BalloonPaymentEnum,
  InterestOnlyPaymentsEnum,
  NegativeAmortizationEnum,
  OtherNonAmortizingFeaturesEnum
}

case class NonAmortizingFeatures(
    balloonPayment: BalloonPaymentEnum,
    interestOnlyPayments: InterestOnlyPaymentsEnum,
    negativeAmortization: NegativeAmortizationEnum,
    otherNonAmortizingFeatures: OtherNonAmortizingFeaturesEnum)
