package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class NonAmortizingFeatures(
                                  balloonPayment: BalloonPaymentEnum = new InvalidBalloonPaymentCode,
                                  interestOnlyPayments: InterestOnlyPaymentsEnum =
                                  new InvalidInterestOnlyPaymentCode,
                                  negativeAmortization: NegativeAmortizationEnum =
                                  new InvalidNegativeArmotizationCode,
                                  otherNonAmortizingFeatures: OtherNonAmortizingFeaturesEnum =
                                  new InvalidOtherNonAmortizingFeaturesCode)

object NonAmortizingFeatures {
      implicit val nonAmortizingFeaturesEncoder: Encoder[NonAmortizingFeatures] =
            (a: NonAmortizingFeatures) =>
                  Json.obj(
                        ("balloonPayment", a.balloonPayment.asInstanceOf[LarEnum].asJson),
                        ("interestOnlyPayment",
                          a.interestOnlyPayments.asInstanceOf[LarEnum].asJson),
                        ("negativeAmortization",
                          a.negativeAmortization.asInstanceOf[LarEnum].asJson),
                        ("otherNonAmortizingFeatures",
                          a.otherNonAmortizingFeatures.asInstanceOf[LarEnum].asJson)
                  )

      implicit val nonAmortizingFeaturesDecoder: Decoder[NonAmortizingFeatures] =
            (c: HCursor) =>
                  for {
                        ballonPayment <- c.downField("balloonPayment").as[Int]
                        interestOnlyPayment <- c.downField("interestOnlyPayment").as[Int]
                        negativeAmortization <- c.downField("negativeAmortization").as[Int]
                        otherNonAmortizingFeatures <- c
                          .downField("otherNonAmortizingFeatures")
                          .as[Int]
                  } yield
                        NonAmortizingFeatures(
                              BalloonPaymentEnum.valueOf(ballonPayment),
                              InterestOnlyPaymentsEnum.valueOf(interestOnlyPayment),
                              NegativeAmortizationEnum.valueOf(negativeAmortization),
                              OtherNonAmortizingFeaturesEnum.valueOf(otherNonAmortizingFeatures)
                        )
}