package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import io.circe._
import io.circe.syntax._

case class Loan(
                 ULI: String = "",
                 applicationDate: String = "",
                 loanType: LoanTypeEnum = new InvalidLoanTypeCode,
                 loanPurpose: LoanPurposeEnum = new InvalidLoanPurposeCode,
                 constructionMethod: ConstructionMethodEnum = new InvalidConstructionMethodCode,
                 occupancy: OccupancyEnum = new InvalidOccupancyCode,
                 amount: BigDecimal = 0.0,
                 loanTerm: String = "NA",
                 rateSpread: String = "NA",
                 interestRate: String = "NA",
                 prepaymentPenaltyTerm: String = "NA",
                 debtToIncomeRatio: String = "NA",
                 combinedLoanToValueRatio: String = "NA",
                 introductoryRatePeriod: String = "NA"
               )

object Loan {
  implicit val loanEncoder: Encoder[Loan] = (a: Loan) =>
    Json.obj(
      ("ULI", Json.fromString(a.ULI)),
      ("applicationDate", Json.fromString(a.applicationDate)),
      ("loanType", a.loanType.asInstanceOf[LarEnum].asJson),
      ("loanPurpose", a.loanPurpose.asInstanceOf[LarEnum].asJson),
      ("constructionMethod", a.constructionMethod.asInstanceOf[LarEnum].asJson),
      ("occupancy", a.occupancy.asInstanceOf[LarEnum].asJson),
      ("amount", Json.fromBigDecimal(a.amount)),
      ("loanTerm", Json.fromString(a.loanTerm)),
      ("rateSpread", Json.fromString(a.rateSpread)),
      ("interestRate", Json.fromString(a.interestRate)),
      ("prepaymentPenaltyTerm", Json.fromString(a.prepaymentPenaltyTerm)),
      ("debtToIncomeRatio", Json.fromString(a.debtToIncomeRatio)),
      ("combinedLoanToValueRatio", Json.fromString(a.combinedLoanToValueRatio)),
      ("introductoryRatePeriod", Json.fromString(a.introductoryRatePeriod))
    )

  implicit val loanDecoder: Decoder[Loan] = (c: HCursor) =>
    for {
      uli <- c.downField("ULI").as[String]
      applicationDate <- c.downField("applicationDate").as[String]
      loanType <- c.downField("loanType").as[Int]
      loanPurpose <- c.downField("loanPurpose").as[Int]
      constructionMethod <- c.downField("constructionMethod").as[Int]
      occupancy <- c.downField("occupancy").as[Int]
      amount <- c.downField("amount").as[BigDecimal]
      loanTerm <- c.downField("loanTerm").as[String]
      rateSpread <- c.downField("rateSpread").as[String]
      interestRate <- c.downField("interestRate").as[String]
      prepaymentPenaltyTerm <- c.downField("prepaymentPenaltyTerm").as[String]
      debtToIncomeRatio <- c.downField("debtToIncomeRatio").as[String]
      combinedLoanToValueRatio <- c
        .downField("combinedLoanToValueRatio")
        .as[String]
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
        combinedLoanToValueRatio,
        introductoryRatePeriod
      )
    }
}