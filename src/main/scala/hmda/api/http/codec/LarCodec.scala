package hmda.api.http.codec

import hmda.model.filing.lar._
import hmda.model.filing.lar.enums._
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

object LarCodec {

  implicit val larEncoder: Encoder[LoanApplicationRegister] =
    new Encoder[LoanApplicationRegister] {
      override def apply(a: LoanApplicationRegister): Json = ???
    }

  implicit val larDecoder: Decoder[LoanApplicationRegister] =
    new Decoder[LoanApplicationRegister] {
      override def apply(c: HCursor): Result[LoanApplicationRegister] = ???
    }

  // Converts all LarEnum to JSON (integer)
  implicit val enumEncoder: Encoder[LarEnum] = new Encoder[LarEnum] {
    override def apply(a: LarEnum): Json = Json.fromInt(a.code)
  }

  implicit val larIdentifierEncoder: Encoder[LarIdentifier] =
    deriveEncoder[LarIdentifier]
  implicit val larIdentifierDecoder: Decoder[LarIdentifier] =
    deriveDecoder[LarIdentifier]

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

  implicit val larActionEncoder: Encoder[LarAction] = new Encoder[LarAction] {
    override def apply(a: LarAction): Json = ???
  }

  implicit val larActionDecoder: Decoder[LarAction] = new Decoder[LarAction] {
    override def apply(c: HCursor): Result[LarAction] = ???
  }

  implicit val geographyEncoder: Encoder[Geography] = new Encoder[Geography] {
    override def apply(a: Geography): Json = ???
  }

  implicit val geographyDecoder: Decoder[Geography] = new Decoder[Geography] {
    override def apply(c: HCursor): Result[Geography] = ???
  }

  implicit val applicantEncoder: Encoder[Applicant] = new Encoder[Applicant] {
    override def apply(a: Applicant): Json = ???
  }

  implicit val applicantDecoder: Decoder[Applicant] = new Decoder[Applicant] {
    override def apply(c: HCursor): Result[Applicant] = ???
  }

  implicit val denialEncoder: Encoder[Denial] = new Encoder[Denial] {
    override def apply(a: Denial): Json = ???
  }

  implicit val denialDecoder: Decoder[Denial] = new Decoder[Denial] {
    override def apply(c: HCursor): Result[Denial] = ???
  }

  implicit val loanDisclosureEncoder: Encoder[LoanDisclosure] =
    new Encoder[LoanDisclosure] {
      override def apply(a: LoanDisclosure): Json = ???
    }

  implicit val loanDisclosureDecoder: Decoder[LoanDisclosure] =
    new Decoder[LoanDisclosure] {
      override def apply(c: HCursor): Result[LoanDisclosure] = ???
    }

  implicit val nonAmortizingFeaturesEncoder: Encoder[NonAmortizingFeatures] =
    new Encoder[NonAmortizingFeatures] {
      override def apply(a: NonAmortizingFeatures): Json = ???
    }

  implicit val nonAmortizingFeaturesDecoder: Decoder[NonAmortizingFeatures] =
    new Decoder[NonAmortizingFeatures] {
      override def apply(c: HCursor): Result[NonAmortizingFeatures] = ???
    }

  implicit val propertyEncoder: Encoder[Property] = new Encoder[Property] {
    override def apply(a: Property): Json = ???
  }

  implicit val propertyDecoder: Decoder[Property] = new Decoder[Property] {
    override def apply(c: HCursor): Result[Property] = ???
  }

  implicit val ausEncoder: Encoder[AutomatedUnderwritingSystem] =
    new Encoder[AutomatedUnderwritingSystem] {
      override def apply(a: AutomatedUnderwritingSystem): Json = ???
    }

  implicit val ausDecoder: Decoder[AutomatedUnderwritingSystem] =
    new Decoder[AutomatedUnderwritingSystem] {
      override def apply(c: HCursor): Result[AutomatedUnderwritingSystem] = ???
    }

  implicit val ausResultEncoder: Encoder[AutomatedUnderwritingSystemResult] =
    new Encoder[AutomatedUnderwritingSystemResult] {
      override def apply(a: AutomatedUnderwritingSystemResult): Json = ???
    }

  implicit val ausResultDecoder: Decoder[AutomatedUnderwritingSystemResult] =
    new Decoder[AutomatedUnderwritingSystemResult] {
      override def apply(
          c: HCursor): Result[AutomatedUnderwritingSystemResult] = ???
    }

}
