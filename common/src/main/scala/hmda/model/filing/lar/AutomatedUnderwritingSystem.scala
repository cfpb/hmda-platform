package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class AutomatedUnderwritingSystem(
                                        aus1: AutomatedUnderwritingSystemEnum =
                                        new InvalidAutomatedUnderwritingSystemCode,
                                        aus2: AutomatedUnderwritingSystemEnum =
                                        new InvalidAutomatedUnderwritingSystemCode,
                                        aus3: AutomatedUnderwritingSystemEnum =
                                        new InvalidAutomatedUnderwritingSystemCode,
                                        aus4: AutomatedUnderwritingSystemEnum =
                                        new InvalidAutomatedUnderwritingSystemCode,
                                        aus5: AutomatedUnderwritingSystemEnum =
                                        new InvalidAutomatedUnderwritingSystemCode,
                                        otherAUS: String = ""
                                      ) extends PipeDelimited {
  override def toCSV: String = {
    s"${aus1.code}|${aus2.code}|${aus3.code}|${aus4.code}|${aus5.code}|$otherAUS"
  }
}

object AutomatedUnderwritingSystem {
  implicit val ausEncoder: Encoder[AutomatedUnderwritingSystem] =
    (a: AutomatedUnderwritingSystem) =>
      Json.obj(
        ("aus1", a.aus1.asInstanceOf[LarEnum].asJson),
        ("aus2", a.aus2.asInstanceOf[LarEnum].asJson),
        ("aus3", a.aus3.asInstanceOf[LarEnum].asJson),
        ("aus4", a.aus4.asInstanceOf[LarEnum].asJson),
        ("aus5", a.aus5.asInstanceOf[LarEnum].asJson),
        ("otherAUS", Json.fromString(a.otherAUS))
      )

  implicit val ausDecoder: Decoder[AutomatedUnderwritingSystem] =
    (c: HCursor) =>
      for {
        aus1 <- c.downField("aus1").as[Int]
        aus2 <- c.downField("aus2").as[Int]
        aus3 <- c.downField("aus3").as[Int]
        aus4 <- c.downField("aus4").as[Int]
        aus5 <- c.downField("aus5").as[Int]
        otherAus <- c.downField("otherAUS").as[String]
      } yield
        AutomatedUnderwritingSystem(
          AutomatedUnderwritingSystemEnum.valueOf(aus1),
          AutomatedUnderwritingSystemEnum.valueOf(aus2),
          AutomatedUnderwritingSystemEnum.valueOf(aus3),
          AutomatedUnderwritingSystemEnum.valueOf(aus4),
          AutomatedUnderwritingSystemEnum.valueOf(aus5),
          otherAus
        )
}