package hmda.api.http.codec.filing

import hmda.model.filing.lar._
import hmda.model.filing.lar.enums._
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

object LarCodec {

  // Converts all LarEnum to JSON (integer)
  implicit val enumEncoder: Encoder[LarEnum] = new Encoder[LarEnum] {
    override def apply(a: LarEnum): Json = Json.fromInt(a.code)
  }

  implicit val larEncoder: Encoder[LoanApplicationRegister] =
    new Encoder[LoanApplicationRegister] {
      override def apply(a: LoanApplicationRegister): Json = Json.obj(
        ("larIdentifier", a.larIdentifier.asJson),
        ("loan", a.loan.asJson),
        ("larAction", a.action.asJson),
        ("geography", a.geography.asJson),
        ("applicant", a.applicant.asJson),
        ("coApplicant", a.coApplicant.asJson),
        ("income", Json.fromString(a.income)),
        ("purchaserType", a.purchaserType.asInstanceOf[LarEnum].asJson),
        ("hoepaStatus", a.hoepaStatus.asInstanceOf[LarEnum].asJson),
        ("lienStatus", a.lienStatus.asInstanceOf[LarEnum].asJson),
        ("denial", a.denial.asJson),
        ("loanDisclosure", a.loanDisclosure.asJson),
        ("nonAmortizingFeatures", a.nonAmortizingFeatures.asJson),
        ("property", a.property.asJson),
        ("applicationSubmission",
         a.applicationSubmission.asInstanceOf[LarEnum].asJson),
        ("payableToInstitution",
         a.payableToInstitution.asInstanceOf[LarEnum].asJson),
        ("AUS", a.AUS.asJson),
        ("ausResult", a.ausResult.asJson),
        ("reverseMortgage", a.reverseMortgage.asInstanceOf[LarEnum].asJson),
        ("lineOfCredit", a.lineOfCredit.asInstanceOf[LarEnum].asJson),
        ("businessOrCommercialPurpose",
         a.businessOrCommercialPurpose.asInstanceOf[LarEnum].asJson)
      )
    }

  implicit val larDecoder: Decoder[LoanApplicationRegister] =
    new Decoder[LoanApplicationRegister] {
      override def apply(c: HCursor): Result[LoanApplicationRegister] =
        for {
          larIdentifier <- c.downField("larIdentifier").as[LarIdentifier]
          loan <- c.downField("loan").as[Loan]
          larAction <- c.downField("larAction").as[LarAction]
          geography <- c.downField("geography").as[Geography]
          applicant <- c.downField("applicant").as[Applicant]
          coApplicant <- c.downField("coApplicant").as[Applicant]
          income <- c.downField("income").as[String]
          purchaserType <- c.downField("purchaserType").as[Int]
          hoepaStatus <- c.downField("hoepaStatus").as[Int]
          lienStatus <- c.downField("lienStatus").as[Int]
          denial <- c.downField("denial").as[Denial]
          loanDisclosure <- c.downField("loanDisclosure").as[LoanDisclosure]
          nonAmortizingFeatures <- c
            .downField("nonAmortizingFeatures")
            .as[NonAmortizingFeatures]
          property <- c.downField("property").as[Property]
          applicationSubmission <- c.downField("applicationSubmission").as[Int]
          payableToInstitution <- c.downField("payableToInstitution").as[Int]
          aus <- c.downField("AUS").as[AutomatedUnderwritingSystem]
          ausResult <- c
            .downField("ausResult")
            .as[AutomatedUnderwritingSystemResult]
          reverseMortgage <- c.downField("reverseMortgage").as[Int]
          lineOfCredit <- c.downField("lineOfCredit").as[Int]
          businessOrCommercialPurpose <- c
            .downField("businessOrCommercialPurpose")
            .as[Int]
        } yield
          LoanApplicationRegister(
            larIdentifier,
            loan,
            larAction,
            geography,
            applicant,
            coApplicant,
            income,
            PurchaserEnum.valueOf(purchaserType),
            HOEPAStatusEnum.valueOf(hoepaStatus),
            LienStatusEnum.valueOf(lienStatus),
            denial,
            loanDisclosure,
            nonAmortizingFeatures,
            property,
            ApplicationSubmissionEnum.valueOf(applicationSubmission),
            PayableToInstitutionEnum.valueOf(payableToInstitution),
            aus,
            ausResult,
            MortgageTypeEnum.valueOf(reverseMortgage),
            LineOfCreditEnum.valueOf(lineOfCredit),
            BusinessOrCommercialBusinessEnum.valueOf(
              businessOrCommercialPurpose)
          )
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
      ("loanToValueRatio", Json.fromString(a.combinedLoanToValueRatio)),
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
    override def apply(a: LarAction): Json = Json.obj(
      ("preapproval", a.preapproval.asInstanceOf[LarEnum].asJson),
      ("actionTakenType", a.actionTakenType.asInstanceOf[LarEnum].asJson),
      ("actionTakenDate", Json.fromInt(a.actionTakenDate))
    )
  }

  implicit val larActionDecoder: Decoder[LarAction] = new Decoder[LarAction] {
    override def apply(c: HCursor): Result[LarAction] =
      for {
        preapproval <- c.downField("preapproval").as[Int]
        actionTakenType <- c.downField("actionTakenType").as[Int]
        actionTakenDate <- c.downField("actionTakenDate").as[Int]
      } yield
        LarAction(
          PreapprovalEnum.valueOf(preapproval),
          ActionTakenTypeEnum.valueOf(actionTakenType),
          actionTakenDate
        )
  }

  implicit val geographyEncoder: Encoder[Geography] = new Encoder[Geography] {
    override def apply(a: Geography): Json = Json.obj(
      ("street", Json.fromString(a.street)),
      ("city", Json.fromString(a.city)),
      ("state", Json.fromString(a.state)),
      ("zipCode", Json.fromString(a.zipCode)),
      ("county", Json.fromString(a.county)),
      ("tract", Json.fromString(a.tract))
    )
  }

  implicit val geographyDecoder: Decoder[Geography] = new Decoder[Geography] {
    override def apply(c: HCursor): Result[Geography] =
      for {
        street <- c.downField("street").as[String]
        city <- c.downField("city").as[String]
        state <- c.downField("state").as[String]
        zipCode <- c.downField("zipCode").as[String]
        county <- c.downField("county").as[String]
        tract <- c.downField("tract").as[String]
      } yield Geography(street, city, state, zipCode, county, tract)
  }

  implicit val ethnicityEncoder: Encoder[Ethnicity] = new Encoder[Ethnicity] {
    override def apply(a: Ethnicity): Json = Json.obj(
      ("ethnicity1", a.ethnicity1.asInstanceOf[LarEnum].asJson),
      ("ethnicity2", a.ethnicity2.asInstanceOf[LarEnum].asJson),
      ("ethnicity3", a.ethnicity3.asInstanceOf[LarEnum].asJson),
      ("ethnicity4", a.ethnicity4.asInstanceOf[LarEnum].asJson),
      ("ethnicity5", a.ethnicity5.asInstanceOf[LarEnum].asJson),
      ("otherHispanicOrLatino", Json.fromString(a.otherHispanicOrLatino)),
      ("ethnicityObserved", a.ethnicityObserved.asInstanceOf[LarEnum].asJson)
    )
  }

  implicit val ethnicityDecoder: Decoder[Ethnicity] = new Decoder[Ethnicity] {
    override def apply(c: HCursor): Result[Ethnicity] =
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

  implicit val raceEncoder: Encoder[Race] = new Encoder[Race] {
    override def apply(a: Race): Json = Json.obj(
      ("race1", a.race1.asInstanceOf[LarEnum].asJson),
      ("race2", a.race2.asInstanceOf[LarEnum].asJson),
      ("race3", a.race3.asInstanceOf[LarEnum].asJson),
      ("race4", a.race4.asInstanceOf[LarEnum].asJson),
      ("race5", a.race5.asInstanceOf[LarEnum].asJson),
      ("otherNativeRace", Json.fromString(a.otherNativeRace)),
      ("otherAsianRace", Json.fromString(a.otherAsianRace)),
      ("otherPacificIslanderRace", Json.fromString(a.otherPacificIslanderRace)),
      ("raceObserved", a.raceObserved.asInstanceOf[LarEnum].asJson)
    )
  }

  implicit val raceDecoder: Decoder[Race] = new Decoder[Race] {
    override def apply(c: HCursor): Result[Race] =
      for {
        race1 <- c.downField("race1").as[Int]
        race2 <- c.downField("race2").as[Int]
        race3 <- c.downField("race3").as[Int]
        race4 <- c.downField("race4").as[Int]
        race5 <- c.downField("race5").as[Int]
        otherNativeRace <- c.downField("otherNativeRace").as[String]
        otherAsianRace <- c.downField("otherAsianRace").as[String]
        otherPacificIslanderRace <- c
          .downField("otherPacificIslanderRace")
          .as[String]
        raceObserved <- c.downField("raceObserved").as[Int]
      } yield
        Race(
          RaceEnum.valueOf(race1),
          RaceEnum.valueOf(race2),
          RaceEnum.valueOf(race3),
          RaceEnum.valueOf(race4),
          RaceEnum.valueOf(race5),
          otherNativeRace,
          otherAsianRace,
          otherPacificIslanderRace,
          RaceObservedEnum.valueOf(raceObserved)
        )
  }

  implicit val sexEncoder: Encoder[Sex] = new Encoder[Sex] {
    override def apply(a: Sex): Json = Json.obj(
      ("sex", a.sexEnum.asInstanceOf[LarEnum].asJson),
      ("sexObserved", a.sexObservedEnum.asInstanceOf[LarEnum].asJson)
    )
  }

  implicit val sexDecoder: Decoder[Sex] = new Decoder[Sex] {
    override def apply(c: HCursor): Result[Sex] =
      for {
        sex <- c.downField("sex").as[Int]
        sexObserved <- c.downField("sexObserved").as[Int]
      } yield Sex(SexEnum.valueOf(sex), SexObservedEnum.valueOf(sexObserved))
  }

  implicit val applicantEncoder: Encoder[Applicant] = new Encoder[Applicant] {
    override def apply(a: Applicant): Json = Json.obj(
      ("ethnicity", a.ethnicity.asJson),
      ("race", a.race.asJson),
      ("sex", a.sex.asJson),
      ("age", Json.fromInt(a.age)),
      ("creditScore", Json.fromInt(a.creditScore)),
      ("creditScoreType", a.creditScoreType.asInstanceOf[LarEnum].asJson),
      ("otherCreditScoreModel", Json.fromString(a.otherCreditScoreModel))
    )
  }

  implicit val applicantDecoder: Decoder[Applicant] = new Decoder[Applicant] {
    override def apply(c: HCursor): Result[Applicant] =
      for {
        ethnicity <- c.downField("ethnicity").as[Ethnicity]
        race <- c.downField("race").as[Race]
        sex <- c.downField("sex").as[Sex]
        age <- c.downField("age").as[Int]
        creditScore <- c.downField("creditScore").as[Int]
        creditScoreType <- c.downField("creditScoreType").as[Int]
        otherCreditScoreModel <- c.downField("otherCreditScoreModel").as[String]
      } yield
        Applicant(
          ethnicity,
          race,
          sex,
          age,
          creditScore,
          CreditScoreEnum.valueOf(creditScoreType),
          otherCreditScoreModel
        )
  }

  implicit val denialEncoder: Encoder[Denial] = new Encoder[Denial] {
    override def apply(a: Denial): Json = Json.obj(
      ("denialReason1", a.denialReason1.asInstanceOf[LarEnum].asJson),
      ("denialReason2", a.denialReason2.asInstanceOf[LarEnum].asJson),
      ("denialReason3", a.denialReason3.asInstanceOf[LarEnum].asJson),
      ("denialReason4", a.denialReason4.asInstanceOf[LarEnum].asJson),
      ("otherDenialReason", Json.fromString(a.otherDenialReason))
    )
  }

  implicit val denialDecoder: Decoder[Denial] = new Decoder[Denial] {
    override def apply(c: HCursor): Result[Denial] =
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

  implicit val loanDisclosureEncoder: Encoder[LoanDisclosure] =
    new Encoder[LoanDisclosure] {
      override def apply(a: LoanDisclosure): Json = Json.obj(
        ("totalLoanCosts", Json.fromString(a.totalLoanCosts)),
        ("totalPointsAndFees", Json.fromString(a.totalPointsAndFees)),
        ("originationCharges", Json.fromString(a.originationCharges)),
        ("discountPoints", Json.fromString(a.discountPoints)),
        ("lenderCredits", Json.fromString(a.lenderCredits))
      )
    }

  implicit val loanDisclosureDecoder: Decoder[LoanDisclosure] =
    new Decoder[LoanDisclosure] {
      override def apply(c: HCursor): Result[LoanDisclosure] =
        for {
          totalLoanCosts <- c.downField("totalLoanCosts").as[String]
          totalPointsAndFees <- c.downField("totalPointsAndFees").as[String]
          originationCharges <- c.downField("originationCharges").as[String]
          discountPoints <- c.downField("discountPoints").as[String]
          lenderCredits <- c.downField("lenderCredits").as[String]
        } yield
          LoanDisclosure(totalLoanCosts,
                         totalPointsAndFees,
                         originationCharges,
                         discountPoints,
                         lenderCredits)
    }

  implicit val nonAmortizingFeaturesEncoder: Encoder[NonAmortizingFeatures] =
    new Encoder[NonAmortizingFeatures] {
      override def apply(a: NonAmortizingFeatures): Json = Json.obj(
        ("balloonPayment", a.balloonPayment.asInstanceOf[LarEnum].asJson),
        ("interestOnlyPayment",
         a.interestOnlyPayments.asInstanceOf[LarEnum].asJson),
        ("negativeAmortization",
         a.negativeAmortization.asInstanceOf[LarEnum].asJson),
        ("otherNonAmortizingFeatures",
         a.otherNonAmortizingFeatures.asInstanceOf[LarEnum].asJson)
      )
    }

  implicit val nonAmortizingFeaturesDecoder: Decoder[NonAmortizingFeatures] =
    new Decoder[NonAmortizingFeatures] {
      override def apply(c: HCursor): Result[NonAmortizingFeatures] =
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

  implicit val propertyEncoder: Encoder[Property] = new Encoder[Property] {
    override def apply(a: Property): Json = Json.obj(
      ("propertyValue", Json.fromString(a.propertyValue)),
      ("manufacturedHomeSecuredProperty",
       a.manufacturedHomeSecuredProperty.asInstanceOf[LarEnum].asJson),
      ("manufacturedHomeLandPropertyInterest",
       a.manufacturedHomeLandPropertyInterest.asInstanceOf[LarEnum].asJson),
      ("totalUnits", Json.fromInt(a.totalUnits)),
      ("multiFamilyAffordableUnits",
       Json.fromString(a.multiFamilyAffordableUnits))
    )
  }

  implicit val propertyDecoder: Decoder[Property] = new Decoder[Property] {
    override def apply(c: HCursor): Result[Property] =
      for {
        propertyValue <- c.downField("propertyValue").as[String]
        manufacturedHomeSecuredProperty <- c
          .downField("manufacturedHomeSecuredProperty")
          .as[Int]
        manufacturedHomeLandPropertyInterest <- c
          .downField("manufacturedHomeLandPropertyInterest")
          .as[Int]
        totalUnits <- c.downField("totalUnits").as[Int]
        multiFamilyAffordableUnits <- c
          .downField("multiFamilyAffordableUnits")
          .as[String]
      } yield
        Property(
          propertyValue,
          ManufacturedHomeSecuredPropertyEnum.valueOf(
            manufacturedHomeSecuredProperty),
          ManufacturedHomeLandPropertyInterestEnum.valueOf(
            manufacturedHomeLandPropertyInterest),
          totalUnits,
          multiFamilyAffordableUnits
        )
  }

  implicit val ausEncoder: Encoder[AutomatedUnderwritingSystem] =
    new Encoder[AutomatedUnderwritingSystem] {
      override def apply(a: AutomatedUnderwritingSystem): Json = Json.obj(
        ("aus1", a.aus1.asInstanceOf[LarEnum].asJson),
        ("aus2", a.aus2.asInstanceOf[LarEnum].asJson),
        ("aus3", a.aus3.asInstanceOf[LarEnum].asJson),
        ("aus4", a.aus4.asInstanceOf[LarEnum].asJson),
        ("aus5", a.aus5.asInstanceOf[LarEnum].asJson),
        ("otherAUS", Json.fromString(a.otherAUS))
      )
    }

  implicit val ausDecoder: Decoder[AutomatedUnderwritingSystem] =
    new Decoder[AutomatedUnderwritingSystem] {
      override def apply(c: HCursor): Result[AutomatedUnderwritingSystem] =
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

  implicit val ausResultEncoder: Encoder[AutomatedUnderwritingSystemResult] =
    new Encoder[AutomatedUnderwritingSystemResult] {
      override def apply(a: AutomatedUnderwritingSystemResult): Json = Json.obj(
        ("ausResult1", a.ausResult1.asInstanceOf[LarEnum].asJson),
        ("ausResult2", a.ausResult2.asInstanceOf[LarEnum].asJson),
        ("ausResult3", a.ausResult3.asInstanceOf[LarEnum].asJson),
        ("ausResult4", a.ausResult4.asInstanceOf[LarEnum].asJson),
        ("ausResult5", a.ausResult5.asInstanceOf[LarEnum].asJson),
        ("otherAusResult", Json.fromString(a.otherAusResult))
      )
    }

  implicit val ausResultDecoder: Decoder[AutomatedUnderwritingSystemResult] =
    new Decoder[AutomatedUnderwritingSystemResult] {
      override def apply(
          c: HCursor): Result[AutomatedUnderwritingSystemResult] =
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

}
