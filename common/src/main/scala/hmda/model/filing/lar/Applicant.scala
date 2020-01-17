package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class Applicant(
  ethnicity: Ethnicity = Ethnicity(),
  race: Race = Race(),
  sex: Sex = Sex(),
  age: Int = 0,
  creditScore: Int = 0,
  creditScoreType: CreditScoreEnum = new InvalidCreditScoreCode,
  otherCreditScoreModel: String = ""
)

object Applicant {
  implicit val applicantEncoder: Encoder[Applicant] = (a: Applicant) =>
    Json.obj(
      ("ethnicity", a.ethnicity.asJson),
      ("race", a.race.asJson),
      ("sex", a.sex.asJson),
      ("age", Json.fromInt(a.age)),
      ("creditScore", Json.fromInt(a.creditScore)),
      ("creditScoreType", a.creditScoreType.asInstanceOf[LarEnum].asJson),
      ("otherCreditScoreModel", Json.fromString(a.otherCreditScoreModel))
    )

  implicit val applicantDecoder: Decoder[Applicant] = (c: HCursor) =>
    for {
      ethnicity             <- c.downField("ethnicity").as[Ethnicity]
      race                  <- c.downField("race").as[Race]
      sex                   <- c.downField("sex").as[Sex]
      age                   <- c.downField("age").as[Int]
      creditScore           <- c.downField("creditScore").as[Int]
      creditScoreType       <- c.downField("creditScoreType").as[Int]
      otherCreditScoreModel <- c.downField("otherCreditScoreModel").as[String]
    } yield Applicant(
      ethnicity,
      race,
      sex,
      age,
      creditScore,
      CreditScoreEnum.valueOf(creditScoreType),
      otherCreditScoreModel
    )
}
