package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class AutomatedUnderwritingSystemResult(
                                              ausResult1: AutomatedUnderwritingResultEnum =
                                              new InvalidAutomatedUnderwritingResultCode,
                                              ausResult2: AutomatedUnderwritingResultEnum =
                                              new InvalidAutomatedUnderwritingResultCode,
                                              ausResult3: AutomatedUnderwritingResultEnum =
                                              new InvalidAutomatedUnderwritingResultCode,
                                              ausResult4: AutomatedUnderwritingResultEnum =
                                              new InvalidAutomatedUnderwritingResultCode,
                                              ausResult5: AutomatedUnderwritingResultEnum =
                                              new InvalidAutomatedUnderwritingResultCode,
                                              otherAusResult: String = ""
                                            ) extends PipeDelimited {
  override def toCSV: String = {
    s"${ausResult1.code}|${ausResult2.code}|${ausResult3.code}|${ausResult4.code}|${ausResult5.code}|$otherAusResult"
  }
}

object AutomatedUnderwritingSystemResult {
  implicit val ausResultEncoder: Encoder[AutomatedUnderwritingSystemResult] =
    (a: AutomatedUnderwritingSystemResult) =>
      Json.obj(
        ("ausResult1", a.ausResult1.asInstanceOf[LarEnum].asJson),
        ("ausResult2", a.ausResult2.asInstanceOf[LarEnum].asJson),
        ("ausResult3", a.ausResult3.asInstanceOf[LarEnum].asJson),
        ("ausResult4", a.ausResult4.asInstanceOf[LarEnum].asJson),
        ("ausResult5", a.ausResult5.asInstanceOf[LarEnum].asJson),
        ("otherAusResult", Json.fromString(a.otherAusResult))
      )

  implicit val ausResultDecoder: Decoder[AutomatedUnderwritingSystemResult] =
    (c: HCursor) =>
      for {
        ausResult1 <- c.downField("ausResult1").as[Int]
        ausResult2 <- c.downField("ausResult2").as[Int]
        ausResult3 <- c.downField("ausResult3").as[Int]
        ausResult4 <- c.downField("ausResult4").as[Int]
        ausResult5 <- c.downField("ausResult5").as[Int]
        otherAusResult <- c.downField("otherAusResult").as[String]
      } yield
        AutomatedUnderwritingSystemResult(
          AutomatedUnderwritingResultEnum.valueOf(ausResult1),
          AutomatedUnderwritingResultEnum.valueOf(ausResult2),
          AutomatedUnderwritingResultEnum.valueOf(ausResult3),
          AutomatedUnderwritingResultEnum.valueOf(ausResult4),
          AutomatedUnderwritingResultEnum.valueOf(ausResult5),
          otherAusResult
        )

}