package hmda.model.filing.lar

import hmda.model.filing.lar.enums._

case class NonAmortizingFeatures(balloonPayment: BalloonPaymentEnum = InvalidBalloonPaymentCode,
                                 interestOnlyPayments: InterestOnlyPaymentsEnum = InvalidInterestOnlyPaymentCode,
                                 negativeAmortization: NegativeAmortizationEnum = InvalidNegativeArmotizationCode,
                                 otherNonAmortizingFeatures: OtherNonAmortizingFeaturesEnum = InvalidOtherNonAmortizingFeaturesCode)
