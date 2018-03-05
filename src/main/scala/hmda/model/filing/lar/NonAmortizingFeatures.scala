package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
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
    extends PipeDelimited {
  override def toCSV: String = {
    s"${balloonPayment.code}|${interestOnlyPayments.code}|${negativeAmortization.code}|${otherNonAmortizingFeatures.code}"
  }
}
