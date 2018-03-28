package hmda.api.http.codec

import hmda.model.filing.lar.{LarIdentifier, Loan}
import hmda.model.filing.lar.enums._
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

object LarCodec {

  implicit val larIdentifierEncoder: Encoder[LarIdentifier] =
    deriveEncoder[LarIdentifier]
  implicit val larIdentifierDecoder: Decoder[LarIdentifier] =
    deriveDecoder[LarIdentifier]

  implicit val enumEncoder: Encoder[LarEnum] = new Encoder[LarEnum] {
    override def apply(a: LarEnum): Json = Json.fromInt(a.code)
  }

  implicit val loanEncoder: Encoder[Loan] = new Encoder[Loan] {
    override def apply(a: Loan): Json = Json.obj(
      ("ULI", Json.fromString(a.ULI)),
      ("applicationDate", Json.fromString(a.applicationDate)),
      ("loanType", a.loanType.asInstanceOf[LarEnum].asJson),
      ("loanPurpose", a.loanPurpose.asInstanceOf[LarEnum].asJson),
      ("constructionMethod", a.constructionMethod.asInstanceOf[LarEnum].asJson),
      ("occupancy", a.occupancy.asInstanceOf[LarEnum].asJson),
      ("amount", Json.fromDoubleOrNull(a.amount)),
      ("loanTerm", Json.fromString(a.loanTerm)),
      ("rateSpread", Json.fromString(a.rateSpread)),
      ("interestRate", Json.fromString(a.interestRate)),
      ("prepaymentPenaltyTerm", Json.fromString(a.prepaymentPenaltyTerm)),
      ("debtToIncomeRatio", Json.fromString(a.debtToIncomeRatio)),
      ("loanToValueRatio", Json.fromString(a.loanToValueRatio)),
      ("introductoryRatePeriod", Json.fromString(a.introductoryRatePeriod))
    )
  }

  implicit val loanDecoder: Decoder[Loan] = new Decoder[Loan] {
    override def apply(c: HCursor): Result[Loan] =
      for {
        uli <- c.downField("ULI").as[String]
        applicationDate <- c.downField("applicationDate").as[String]
        loanType <- c.downField("loanType").as[Int]
        loanPurpose <- c.downField("loanPurpose").as[Int]
        constructionMethod <- c.downField("constructionMethod").as[Int]
        occupancy <- c.downField("occupancy").as[Int]
        amount <- c.downField("amount").as[Double]
        loanTerm <- c.downField("loanTerm").as[String]
        rateSpread <- c.downField("rateSpread").as[String]
        interestRate <- c.downField("interestRate").as[String]
        prepaymentPenaltyTerm <- c.downField("prepaymentPenaltyTerm").as[String]
        debtToIncomeRatio <- c.downField("debtToIncomeRatio").as[String]
        loanToValueRatio <- c.downField("loanToValueRatio").as[String]
        introductoryRatePeriod <- c
          .downField("introductoryRatePeriod")
          .as[String]
      } yield {
        Loan(
          uli,
          applicationDate,
          LoanTypeEnum.valueOf(loanType),
          LoanPurposeEnum.valueOf(loanPurpose),
          ConstructionMethodEnum.valueOf(constructionMethod),
          OccupancyEnum.valueOf(occupancy),
          amount,
          loanTerm,
          rateSpread,
          interestRate,
          prepaymentPenaltyTerm,
          debtToIncomeRatio,
          loanToValueRatio,
          introductoryRatePeriod
        )
      }
  }

}
