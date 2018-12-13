package hmda.publication.lar.parser

import hmda.model.filing.lar.enums._
import hmda.model.filing.lar._
import hmda.publication.lar.model.ModifiedLoanApplicationRegister

import Math._

object ModifiedLarCsvParser {

  def apply(s: String): ModifiedLoanApplicationRegister = {
    convert(parseLar(s))
  }

  private def parseLar(s: String): LoanApplicationRegister = {
    val values = s.split('|').map(_.trim).toList
    val id = values(0).toInt
    val lei = values(1)
    val uli = values(2)
    val applicationDate = values(3)
    val loanType = values(4).toInt
    val loanPurpose = values(5).toInt
    val preapproval = values(6).toInt
    val constructionMethod = values(7).toInt
    val occupancy = values(8).toInt
    val loanAmount = values(9).toInt
    val actionTaken = values(10).toInt
    val actionTakenDate = values(11).toInt
    val street = values(12)
    val city = values(13)
    val state = values(14)
    val zipCode = values(15)
    val county = values(16)
    val tract = values(17)
    val appEth1 = values(18).toInt
    val appEth2 = values(19).toInt
    val appEth3 = values(20).toInt
    val appEth4 = values(21).toInt
    val appEth5 = values(22).toInt
    val appEthOther = values(23)
    val coAppEth1 = values(24).toInt
    val coAppEth2 = values(25).toInt
    val coAppEth3 = values(26).toInt
    val coAppEth4 = values(27).toInt
    val coAppEth5 = values(28).toInt
    val coAppEthOther = values(29)
    val appEthObserved = values(30).toInt
    val coAppEthObserved = values(31).toInt
    val appRace1 = values(32).toInt
    val appRace2 = values(33).toInt
    val appRace3 = values(34).toInt
    val appRace4 = values(35).toInt
    val appRace5 = values(36).toInt
    val appOtherNative = values(37)
    val appOtherAsian = values(38)
    val appOtherPacific = values(39)
    val coAppRace1 = values(40).toInt
    val coAppRace2 = values(41).toInt
    val coAppRace3 = values(42).toInt
    val coAppRace4 = values(43).toInt
    val coAppRace5 = values(44).toInt
    val coAppOtherNative = values(45)
    val coAppOtherAsian = values(46)
    val coAppOtherPacific = values(47)
    val appRaceObserved = values(48).toInt
    val coAppRaceObserved = values(49).toInt
    val appSex = values(50).toInt
    val coAppSex = values(51).toInt
    val appSexObserved = values(52).toInt
    val coAppSexObserved = values(53).toInt
    val appAge = values(54).toInt
    val coAppAge = values(55).toInt
    val income = values(56)
    val purchaserType = values(57).toInt
    val rateSpread = values(58)
    val hoepaStatus = values(59).toInt
    val lienStatus = values(60).toInt
    val appCreditScore = values(61).toInt
    val coAppCreditScore = values(62).toInt
    val appCreditScoreModel = values(63).toInt
    val appCreditScoreModelOther = values(64)
    val coAppCreditScoreModel = values(65).toInt
    val coAppCreditScoreModelOther = values(66)
    val denial1 = values(67).toInt
    val denial2 = values(68).toInt
    val denial3 = values(69).toInt
    val denial4 = values(70).toInt
    val denialOther = values(71)
    val totalLoanCosts = values(72)
    val totalPointsAndFees = values(73)
    val originationCharges = values(74)
    val discountPoints = values(75)
    val lenderCredits = values(76)
    val interestRate = values(77)
    val prepaymentPenaltyTerm = values(78)
    val debtToIncomeRatio = converDebtToIncomeRatio(values(79))
    val loanToValueRatio = values(80)
    val loanTerm = values(81)
    val introductoryRatePeriod = values(82)
    val balloonPayment = values(83).toInt
    val interestOnlyPayment = values(84).toInt
    val negativeAmortization = values(85).toInt
    val otherNonAmortizingFeatures = values(86).toInt
    val propertyValue = values(87)
    val manufacturedHomeSecuredProperty = values(88).toInt
    val manufacturedHomeLandPropertyInterest = values(89).toInt
    val totalUnits = values(90).toInt
    val multifamilyAffordableUnits = values(91)
    val submissionOfApplication = values(92).toInt
    val payableToInstitution = values(93).toInt
    val nmlsrIdentifier = values(94)
    val aus1 = values(95).toInt
    val aus2 = values(96).toInt
    val aus3 = values(97).toInt
    val aus4 = values(98).toInt
    val aus5 = values(99).toInt
    val ausOther = values(100)
    val ausResult1 = values(101).toInt
    val ausResult2 = values(102).toInt
    val ausResult3 = values(103).toInt
    val ausResult4 = values(104).toInt
    val ausResult5 = values(105).toInt
    val ausResultOther = values(106)
    val reverseMortgage = values(107).toInt
    val openEndLineOfCredit = values(108).toInt
    val businessOrCommercial = values(109).toInt

    val larIdentifier = LarIdentifier(id, lei, nmlsrIdentifier)
    val loan = Loan(
      uli,
      applicationDate,
      LoanTypeEnum.valueOf(loanType),
      LoanPurposeEnum.valueOf(loanPurpose),
      ConstructionMethodEnum.valueOf(constructionMethod),
      OccupancyEnum.valueOf(occupancy),
      loanAmount,
      loanTerm,
      rateSpread,
      interestRate,
      prepaymentPenaltyTerm,
      debtToIncomeRatio,
      loanToValueRatio,
      introductoryRatePeriod
    )

    val larAction = LarAction(PreapprovalEnum.valueOf(preapproval),
                              ActionTakenTypeEnum.valueOf(actionTaken),
                              actionTakenDate)

    val geography = Geography(street, city, state, zipCode, county, tract)

    val applicantEthnicity = Ethnicity(
      EthnicityEnum.valueOf(appEth1),
      EthnicityEnum.valueOf(appEth2),
      EthnicityEnum.valueOf(appEth3),
      EthnicityEnum.valueOf(appEth4),
      EthnicityEnum.valueOf(appEth5),
      appEthOther,
      EthnicityObservedEnum.valueOf(appEthObserved),
    )

    val coApplicantEthnicity = Ethnicity(
      EthnicityEnum.valueOf(coAppEth1),
      EthnicityEnum.valueOf(coAppEth2),
      EthnicityEnum.valueOf(coAppEth3),
      EthnicityEnum.valueOf(coAppEth4),
      EthnicityEnum.valueOf(coAppEth5),
      coAppEthOther,
      EthnicityObservedEnum.valueOf(coAppEthObserved),
    )

    val applicantRace = Race(
      RaceEnum.valueOf(appRace1),
      RaceEnum.valueOf(appRace2),
      RaceEnum.valueOf(appRace3),
      RaceEnum.valueOf(appRace4),
      RaceEnum.valueOf(appRace5),
      appOtherNative,
      appOtherAsian,
      appOtherPacific,
      RaceObservedEnum.valueOf(appRaceObserved),
    )

    val coApplicantRace = Race(
      RaceEnum.valueOf(coAppRace1),
      RaceEnum.valueOf(coAppRace2),
      RaceEnum.valueOf(coAppRace3),
      RaceEnum.valueOf(coAppRace4),
      RaceEnum.valueOf(coAppRace5),
      coAppOtherNative,
      coAppOtherAsian,
      coAppOtherPacific,
      RaceObservedEnum.valueOf(coAppRaceObserved),
    )

    val applicantSex =
      Sex(SexEnum.valueOf(appSex), SexObservedEnum.valueOf(appSexObserved))

    val coApplicantSex =
      Sex(SexEnum.valueOf(coAppSex), SexObservedEnum.valueOf(coAppSexObserved))

    val applicant = Applicant(
      applicantEthnicity,
      applicantRace,
      applicantSex,
      appAge,
      appCreditScore,
      CreditScoreEnum.valueOf(appCreditScoreModel),
      appCreditScoreModelOther
    )

    val coApplicant = Applicant(
      coApplicantEthnicity,
      coApplicantRace,
      coApplicantSex,
      coAppAge,
      coAppCreditScore,
      CreditScoreEnum.valueOf(coAppCreditScoreModel),
      coAppCreditScoreModelOther
    )

    val denial = Denial(DenialReasonEnum.valueOf(denial1),
                        DenialReasonEnum.valueOf(denial2),
                        DenialReasonEnum.valueOf(denial3),
                        DenialReasonEnum.valueOf(denial4),
                        denialOther)

    val loanDisclosure = LoanDisclosure(totalLoanCosts,
                                        totalPointsAndFees,
                                        originationCharges,
                                        discountPoints,
                                        lenderCredits)

    val nonAmortizingFeatures = NonAmortizingFeatures(
      BalloonPaymentEnum.valueOf(balloonPayment),
      InterestOnlyPaymentsEnum.valueOf(interestOnlyPayment),
      NegativeAmortizationEnum.valueOf(negativeAmortization),
      OtherNonAmortizingFeaturesEnum.valueOf(otherNonAmortizingFeatures)
    )

    val property = Property(
      propertyValue,
      ManufacturedHomeSecuredPropertyEnum.valueOf(
        manufacturedHomeSecuredProperty),
      ManufacturedHomeLandPropertyInterestEnum.valueOf(
        manufacturedHomeLandPropertyInterest),
      totalUnits,
      multifamilyAffordableUnits
    )

    val aus = AutomatedUnderwritingSystem(
      AutomatedUnderwritingSystemEnum.valueOf(aus1),
      AutomatedUnderwritingSystemEnum.valueOf(aus2),
      AutomatedUnderwritingSystemEnum.valueOf(aus3),
      AutomatedUnderwritingSystemEnum.valueOf(aus4),
      AutomatedUnderwritingSystemEnum.valueOf(aus5),
      ausOther
    )

    val ausResult = AutomatedUnderwritingSystemResult(
      AutomatedUnderwritingResultEnum.valueOf(ausResult1),
      AutomatedUnderwritingResultEnum.valueOf(ausResult2),
      AutomatedUnderwritingResultEnum.valueOf(ausResult3),
      AutomatedUnderwritingResultEnum.valueOf(ausResult4),
      AutomatedUnderwritingResultEnum.valueOf(ausResult5),
      ausResultOther
    )

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
      ApplicationSubmissionEnum.valueOf(submissionOfApplication),
      PayableToInstitutionEnum.valueOf(payableToInstitution),
      aus,
      ausResult,
      MortgageTypeEnum.valueOf(reverseMortgage),
      LineOfCreditEnum.valueOf(openEndLineOfCredit),
      BusinessOrCommercialBusinessEnum.valueOf(businessOrCommercial)
    )
  }

  //TODO: perform final conversion (i.e. binning)
  private def convert(
      lar: LoanApplicationRegister): ModifiedLoanApplicationRegister = {
    ModifiedLoanApplicationRegister(
      lar.larIdentifier.id,
      lar.larIdentifier.LEI,
      lar.loan.loanType.code,
      lar.loan.loanPurpose.code,
      lar.action.preapproval.code,
      lar.loan.constructionMethod.code,
      lar.loan.occupancy.code,
      roundToMidPoint(lar.loan.amount.toInt),
      lar.action.actionTakenType.code,
      lar.geography.state,
      lar.geography.county,
      lar.geography.tract,
      lar.applicant.ethnicity.ethnicity1.code.toString,
      lar.applicant.ethnicity.ethnicity2.code.toString,
      lar.applicant.ethnicity.ethnicity3.code.toString,
      lar.applicant.ethnicity.ethnicity4.code.toString,
      lar.applicant.ethnicity.ethnicity5.code.toString,
      lar.applicant.ethnicity.ethnicityObserved.code,
      lar.coApplicant.ethnicity.ethnicity1.code.toString,
      lar.coApplicant.ethnicity.ethnicity2.code.toString,
      lar.coApplicant.ethnicity.ethnicity3.code.toString,
      lar.coApplicant.ethnicity.ethnicity4.code.toString,
      lar.coApplicant.ethnicity.ethnicity5.code.toString,
      lar.coApplicant.ethnicity.ethnicityObserved.code,
      lar.applicant.race.race1.code.toString,
      lar.applicant.race.race2.code.toString,
      lar.applicant.race.race3.code.toString,
      lar.applicant.race.race4.code.toString,
      lar.applicant.race.race5.code.toString,
      lar.coApplicant.race.race1.code.toString,
      lar.coApplicant.race.race2.code.toString,
      lar.coApplicant.race.race3.code.toString,
      lar.coApplicant.race.race1.code.toString,
      lar.coApplicant.race.race5.code.toString,
      lar.applicant.race.raceObserved.code,
      lar.coApplicant.race.raceObserved.code,
      lar.applicant.sex.sexEnum.code,
      lar.coApplicant.sex.sexEnum.code,
      lar.applicant.sex.sexObservedEnum.code,
      lar.coApplicant.sex.sexObservedEnum.code,
      convertAge(lar.applicant.age),
      isAgeGreaterThan62(lar.applicant.age),
      convertAge(lar.coApplicant.age),
      isAgeGreaterThan62(lar.coApplicant.age),
      lar.income,
      lar.purchaserType.code,
      lar.loan.rateSpread,
      lar.hoepaStatus.code,
      lar.lienStatus.code,
      lar.applicant.creditScoreType.code,
      lar.coApplicant.creditScoreType.code,
      lar.denial.denialReason1.code,
      lar.denial.denialReason2.code.toString,
      lar.denial.denialReason3.code.toString,
      lar.denial.denialReason4.code.toString,
      lar.loanDisclosure.totalLoanCosts,
      lar.loanDisclosure.totalPointsAndFees,
      lar.loanDisclosure.originationCharges,
      lar.loanDisclosure.discountPoints,
      lar.loanDisclosure.lenderCredits,
      lar.loan.interestRate,
      lar.loan.prepaymentPenaltyTerm,
      lar.loan.debtToIncomeRatio,
      lar.loan.combinedLoanToValueRatio,
      lar.loan.loanTerm,
      lar.loan.introductoryRatePeriod,
      lar.nonAmortizingFeatures.balloonPayment.code,
      lar.nonAmortizingFeatures.interestOnlyPayments.code,
      lar.nonAmortizingFeatures.negativeAmortization.code,
      lar.nonAmortizingFeatures.otherNonAmortizingFeatures.code,
      convertPropertyValue(lar.property.propertyValue),
      lar.property.manufacturedHomeSecuredProperty.code,
      lar.property.manufacturedHomeLandPropertyInterest.code,
      convertTotalUnits(lar.property.totalUnits),
      convertMultifamilyAffordableUnits(lar.property.multiFamilyAffordableUnits,
                                        lar.property.totalUnits),
      lar.applicationSubmission.code,
      lar.payableToInstitution.code,
      lar.AUS.aus1.code,
      lar.AUS.aus2.code,
      lar.AUS.aus3.code,
      lar.AUS.aus4.code,
      lar.AUS.aus5.code,
      lar.reverseMortgage.code,
      lar.lineOfCredit.code,
      lar.businessOrCommercialPurpose.code
    )
  }

  private def convertAge(age: Int): String = age match {
    case x if 0 until 25 contains x => "<25"
    case x if 25 to 34 contains x   => "25-34"
    case x if 35 to 44 contains x   => "35-44"
    case x if 45 to 54 contains x   => "45-54"
    case x if 55 to 64 contains x   => "55-64"
    case x if 65 to 74 contains x   => "65-74"
    case x if x > 74                => ">74"
  }

  private def isAgeGreaterThan62(age: Int): String = age match {
    case x if x == 8888 || x == 9999 => "NA"
    case x if x >= 62                => "Yes"
    case x if x < 62                 => "No"
  }

  private def converDebtToIncomeRatio(ratio: String): String = ratio match {
    case x if x == "NA" || x == "Exempt" => x
    case _ =>
      ratio.toInt match {
        case x if x < 20                 => "<20%"
        case x if 20 until 30 contains x => "20-30%"
        case x if 30 until 36 contains x => "30-36%"
        case x if 36 until 50 contains x => x.toString
        case x if 50 until 60 contains x => "50-60%"
        case x if x >= 60                => "+60%"
      }
  }

  private def convertTotalUnits(totalUnits: Int): String = totalUnits match {
    case x if x < 5                 => x.toString
    case x if 5 to 23 contains x    => "5-23"
    case x if 24 to 49 contains x   => "24-49"
    case x if 50 to 99 contains x   => "50-99"
    case x if 100 to 149 contains x => "100-149"
    case x if x >= 150              => "150+"
  }

  private def convertMultifamilyAffordableUnits(multifamilyUnits: String,
                                                totalUnits: Int): String =
    multifamilyUnits match {
      case x if x == "NA" || x == "Exempt" => x
      case _ =>
        val percentage = (multifamilyUnits.toFloat / totalUnits.toFloat) * 100
        round(percentage).toString
    }

  private def convertPropertyValue(propertyValue: String): String =
    propertyValue match {
      case x if x == "NA" || x == "Exempt" => x
      case x                               => roundToMidPoint(x.toInt).toString
    }

  private def roundToMidPoint(x: Int): Int = {
    val rounded = 10000 * Math.floor(x / 10000) + 5000
    rounded.toInt
  }

}
