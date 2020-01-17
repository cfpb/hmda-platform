package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class Denial(
                   denialReason1: DenialReasonEnum = new InvalidDenialReasonCode,
                   denialReason2: DenialReasonEnum = new InvalidDenialReasonCode,
                   denialReason3: DenialReasonEnum = new InvalidDenialReasonCode,
                   denialReason4: DenialReasonEnum = new InvalidDenialReasonCode,
                   otherDenialReason: String = ""
                 )

object Denial {
  implicit val denialEncoder: Encoder[Denial] = (a: Denial) =>
    Json.obj(
      ("denialReason1", a.denialReason1.asInstanceOf[LarEnum].asJson),
      ("denialReason2", a.denialReason2.asInstanceOf[LarEnum].asJson),
      ("denialReason3", a.denialReason3.asInstanceOf[LarEnum].asJson),
      ("denialReason4", a.denialReason4.asInstanceOf[LarEnum].asJson),
      ("otherDenialReason", Json.fromString(a.otherDenialReason))
    )

  implicit val denialDecoder: Decoder[Denial] = (c: HCursor) =>
    for {
      denialReason1 <- c.downField("denialReason1").as[Int]
      denialReason2 <- c.downField("denialReason2").as[Int]
      denialReason3 <- c.downField("denialReason3").as[Int]
      denialReason4 <- c.downField("denialReason4").as[Int]
      otherDenialReason <- c.downField("otherDenialReason").as[String]
    } yield
      Denial(
        DenialReasonEnum.valueOf(denialReason1),
        DenialReasonEnum.valueOf(denialReason2),
        DenialReasonEnum.valueOf(denialReason3),
        DenialReasonEnum.valueOf(denialReason4),
        otherDenialReason
      )
}