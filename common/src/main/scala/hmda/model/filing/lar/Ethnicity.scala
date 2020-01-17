package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class Ethnicity(
                      ethnicity1: EthnicityEnum = new InvalidEthnicityCode,
                      ethnicity2: EthnicityEnum = new InvalidEthnicityCode,
                      ethnicity3: EthnicityEnum = new InvalidEthnicityCode,
                      ethnicity4: EthnicityEnum = new InvalidEthnicityCode,
                      ethnicity5: EthnicityEnum = new InvalidEthnicityCode,
                      otherHispanicOrLatino: String = "",
                      ethnicityObserved: EthnicityObservedEnum = new InvalidEthnicityObservedCode
                    )

object Ethnicity {
  implicit val ethnicityEncoder: Encoder[Ethnicity] = (a: Ethnicity) =>
    Json.obj(
      ("ethnicity1", a.ethnicity1.asInstanceOf[LarEnum].asJson),
      ("ethnicity2", a.ethnicity2.asInstanceOf[LarEnum].asJson),
      ("ethnicity3", a.ethnicity3.asInstanceOf[LarEnum].asJson),
      ("ethnicity4", a.ethnicity4.asInstanceOf[LarEnum].asJson),
      ("ethnicity5", a.ethnicity5.asInstanceOf[LarEnum].asJson),
      ("otherHispanicOrLatino", Json.fromString(a.otherHispanicOrLatino)),
      ("ethnicityObserved", a.ethnicityObserved.asInstanceOf[LarEnum].asJson)
    )

  implicit val ethnicityDecoder: Decoder[Ethnicity] = (c: HCursor) =>
    for {
      ethnicity1 <- c.downField("ethnicity1").as[Int]
      ethnicity2 <- c.downField("ethnicity2").as[Int]
      ethnicity3 <- c.downField("ethnicity3").as[Int]
      ethnicity4 <- c.downField("ethnicity4").as[Int]
      ethnicity5 <- c.downField("ethnicity5").as[Int]
      otherHispanicOrLatino <- c.downField("otherHispanicOrLatino").as[String]
      ethnicityObserved <- c.downField("ethnicityObserved").as[Int]
    } yield
      Ethnicity(
        EthnicityEnum.valueOf(ethnicity1),
        EthnicityEnum.valueOf(ethnicity2),
        EthnicityEnum.valueOf(ethnicity3),
        EthnicityEnum.valueOf(ethnicity4),
        EthnicityEnum.valueOf(ethnicity5),
        otherHispanicOrLatino,
        EthnicityObservedEnum.valueOf(ethnicityObserved)
      )
}