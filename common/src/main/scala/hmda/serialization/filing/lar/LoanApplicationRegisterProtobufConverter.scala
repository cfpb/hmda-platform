package hmda.serialization.filing.lar

import hmda.model.filing.lar._
import hmda.model.filing.lar.enums._
import hmda.persistence.serialization.loanapplicationregister._

object LoanApplicationRegisterProtobufConverter {

  def larIdentifierToProtobuf(larIdentifier: LarIdentifier): LarIdentifierMessage =
    LarIdentifierMessage(larIdentifier.id, larIdentifier.LEI, larIdentifier.NMLSRIdentifier)

  def larIdentifierFromProtobuf(msg: LarIdentifierMessage): LarIdentifier =
    LarIdentifier(msg.id, msg.lEI, msg.nMLSRIdentifier)

  def loanToProtobuf(loan: Loan): LoanMessage =
    LoanMessage(
      loan.ULI,
      loan.applicationDate,
      loan.loanType.code,
      loan.loanPurpose.code,
      loan.constructionMethod.code,
      loan.occupancy.code,
      loan.amount.toDouble,
      loan.loanTerm,
      loan.rateSpread,
      loan.interestRate,
      loan.prepaymentPenaltyTerm,
      loan.debtToIncomeRatio,
      loan.combinedLoanToValueRatio,
      loan.introductoryRatePeriod
    )

  def loanFromProtobuf(msg: LoanMessage): Loan = {
    val loanType    = LoanTypeEnum.valueOf(msg.loanType)
    val loanPurpose = LoanPurposeEnum.valueOf(msg.loanPurpose)
    val constructionMethod =
      ConstructionMethodEnum.valueOf(msg.constructionMethod)
    val occupancy = OccupancyEnum.valueOf(msg.occupancy)

    Loan(
      msg.uLI,
      msg.applicationDate,
      loanType,
      loanPurpose,
      constructionMethod,
      occupancy,
      msg.amount,
      msg.loanTerm,
      msg.rateSpread,
      msg.interestRate,
      msg.prepaymentPenaltyTerm,
      msg.debtToIncomeRatio,
      msg.combinedLoanToValueRatio,
      msg.introductoryRatePeriod
    )
  }

  def larActionToProtobuf(larAction: LarAction): LarActionMessage =
    LarActionMessage(larAction.preapproval.code, larAction.actionTakenType.code, larAction.actionTakenDate)

  def larActionFromProtobuf(msg: LarActionMessage): LarAction = {
    val preapproval     = PreapprovalEnum.valueOf(msg.preapproval)
    val actionTakenType = ActionTakenTypeEnum.valueOf(msg.actionTakenType)

    LarAction(preapproval, actionTakenType, msg.actionTakenDate)
  }

  def geographyToProtobuf(geography: Geography): GeographyMessage =
    GeographyMessage(geography.street, geography.city, geography.state, geography.zipCode, geography.county, geography.tract)

  def geographyFromProtobuf(msg: GeographyMessage): Geography =
    Geography(msg.street, msg.city, msg.state, msg.zipCode, msg.county, msg.tract)

  def applicantToProtobuf(applicant: Applicant): ApplicantMessage = {
    val ethnicity = ethnicityToProtobuf(applicant.ethnicity)
    val race      = raceToProtobuf(applicant.race)
    val sex       = sexToProtobuf(applicant.sex)

    ApplicantMessage(Some(ethnicity),
                     Some(race),
                     Some(sex),
                     applicant.age,
                     applicant.creditScore,
                     applicant.creditScoreType.code,
                     applicant.otherCreditScoreModel)
  }

  def applicantFromProtobuf(msg: ApplicantMessage): Applicant = {
    val ethnicity        = ethnicityFromProtobuf(msg.ethnicty.getOrElse(EthnicityMessage()))
    val race             = raceFromProtobuf(msg.race.getOrElse(RaceMessage()))
    val sex              = sexFromProtobuf(msg.sex.getOrElse(SexMessage()))
    val creditScoreModel = CreditScoreEnum.valueOf(msg.creditScoreType)

    Applicant(ethnicity, race, sex, msg.age, msg.creditScore, creditScoreModel, msg.otherCreditScoreModel)
  }

  def ethnicityToProtobuf(ethnicity: Ethnicity): EthnicityMessage =
    EthnicityMessage(
      ethnicity.ethnicity1.code,
      ethnicity.ethnicity2.code,
      ethnicity.ethnicity3.code,
      ethnicity.ethnicity4.code,
      ethnicity.ethnicity5.code,
      ethnicity.otherHispanicOrLatino,
      ethnicity.ethnicityObserved.code
    )

  def ethnicityFromProtobuf(msg: EthnicityMessage): Ethnicity = {
    val ethnicity1        = EthnicityEnum.valueOf(msg.ethnicity1)
    val ethnicity2        = EthnicityEnum.valueOf(msg.ethnicity2)
    val ethnicity3        = EthnicityEnum.valueOf(msg.ethnicity3)
    val ethnicity4        = EthnicityEnum.valueOf(msg.ethnicity4)
    val ethnicity5        = EthnicityEnum.valueOf(msg.ethnicity5)
    val ethnicityObserved = EthnicityObservedEnum.valueOf(msg.ethnicityObserved)

    Ethnicity(ethnicity1, ethnicity2, ethnicity3, ethnicity4, ethnicity5, msg.otherHispanicOrLatino, ethnicityObserved)
  }

  def raceToProtobuf(race: Race): RaceMessage =
    RaceMessage(
      race.race1.code,
      race.race2.code,
      race.race3.code,
      race.race4.code,
      race.race5.code,
      race.otherNativeRace,
      race.otherAsianRace,
      race.otherPacificIslanderRace,
      race.raceObserved.code
    )

  def raceFromProtobuf(msg: RaceMessage): Race = {
    val race1        = RaceEnum.valueOf(msg.race1)
    val race2        = RaceEnum.valueOf(msg.race2)
    val race3        = RaceEnum.valueOf(msg.race3)
    val race4        = RaceEnum.valueOf(msg.race4)
    val race5        = RaceEnum.valueOf(msg.race5)
    val raceObserved = RaceObservedEnum.valueOf(msg.raceObserved)

    Race(race1, race2, race3, race4, race5, msg.otherNativeRace, msg.otherAsianRace, msg.otherPacificIslanderRace, raceObserved)
  }

  def sexToProtobuf(sex: Sex): SexMessage =
    SexMessage(sex.sexEnum.code, sex.sexObservedEnum.code)

  def sexFromProtobuf(msg: SexMessage): Sex = {
    val sex         = SexEnum.valueOf(msg.sexEnum)
    val sexObserved = SexObservedEnum.valueOf(msg.sexObservedEnum)

    Sex(sex, sexObserved)
  }

  def denialToProtobuf(denial: Denial): DenialMessage =
    DenialMessage(denial.denialReason1.code,
                  denial.denialReason2.code,
                  denial.denialReason3.code,
                  denial.denialReason4.code,
                  denial.otherDenialReason)

  def denialFromProtobuf(msg: DenialMessage): Denial = {
    val denial1 = DenialReasonEnum.valueOf(msg.denialReason1)
    val denial2 = DenialReasonEnum.valueOf(msg.denialReason2)
    val denial3 = DenialReasonEnum.valueOf(msg.denialReason3)
    val denial4 = DenialReasonEnum.valueOf(msg.denialReason4)

    Denial(denial1, denial2, denial3, denial4, msg.otherDenialReason)
  }

  def loanDisclosureToProtobuf(loanDisclosure: LoanDisclosure): LoanDisclosureMessage =
    LoanDisclosureMessage(
      loanDisclosure.totalLoanCosts,
      loanDisclosure.totalPointsAndFees,
      loanDisclosure.originationCharges,
      loanDisclosure.discountPoints,
      loanDisclosure.lenderCredits
    )

  def loanDisclosureFromProtobuf(msg: LoanDisclosureMessage): LoanDisclosure =
    LoanDisclosure(msg.totalLoanCosts, msg.totalPointsAndFees, msg.originationCharges, msg.discountPoints, msg.lenderCredits)

  def nonAmortizingFeaturesToProtobuf(nonAmortizingFeatures: NonAmortizingFeatures): NonAmortizingFeaturesMessage =
    NonAmortizingFeaturesMessage(
      nonAmortizingFeatures.balloonPayment.code,
      nonAmortizingFeatures.interestOnlyPayments.code,
      nonAmortizingFeatures.negativeAmortization.code,
      nonAmortizingFeatures.otherNonAmortizingFeatures.code
    )

  def nonAmortizingFeaturesFromProtobuf(msg: NonAmortizingFeaturesMessage): NonAmortizingFeatures = {
    val balloonPayment = BalloonPaymentEnum.valueOf(msg.balloonPayment)
    val interestOnlyPayment =
      InterestOnlyPaymentsEnum.valueOf(msg.interestOnlyPayments)
    val negativeAmortization =
      NegativeAmortizationEnum.valueOf(msg.negativeAmortization)
    val otherNonAmortizingFeatures =
      OtherNonAmortizingFeaturesEnum.valueOf(msg.otherNonAmortizingFeatures)

    NonAmortizingFeatures(balloonPayment, interestOnlyPayment, negativeAmortization, otherNonAmortizingFeatures)
  }

  def propertyToProtobuf(property: Property): PropertyMessage =
    PropertyMessage(
      property.propertyValue,
      property.manufacturedHomeSecuredProperty.code,
      property.manufacturedHomeLandPropertyInterest.code,
      property.totalUnits,
      property.multiFamilyAffordableUnits
    )

  def propertyFromProtobuf(msg: PropertyMessage): Property = {
    val manufacturedHomeSecuredProperty =
      ManufacturedHomeSecuredPropertyEnum.valueOf(msg.manufacturedHomeSecuredProperty)
    val manufacturedHomeLandPropertyInterest =
      ManufacturedHomeLandPropertyInterestEnum.valueOf(msg.manufacturedHomeLandPropertyInterest)

    Property(msg.propertyValue,
             manufacturedHomeSecuredProperty,
             manufacturedHomeLandPropertyInterest,
             msg.totalUnits,
             msg.multiFamilyAffordableUnits)
  }

  def ausToProtobuf(aus: AutomatedUnderwritingSystem): AusMessage =
    AusMessage(aus.aus1.code, aus.aus2.code, aus.aus3.code, aus.aus4.code, aus.aus5.code, aus.otherAUS)

  def ausFromProtobuf(msg: AusMessage): AutomatedUnderwritingSystem = {
    val aus1 = AutomatedUnderwritingSystemEnum.valueOf(msg.aus1)
    val aus2 = AutomatedUnderwritingSystemEnum.valueOf(msg.aus2)
    val aus3 = AutomatedUnderwritingSystemEnum.valueOf(msg.aus3)
    val aus4 = AutomatedUnderwritingSystemEnum.valueOf(msg.aus4)
    val aus5 = AutomatedUnderwritingSystemEnum.valueOf(msg.aus5)

    AutomatedUnderwritingSystem(aus1, aus2, aus3, aus4, aus5, msg.otherAUS)
  }

  def ausResultToProtobuf(ausResult: AutomatedUnderwritingSystemResult): AusResultMessage =
    AusResultMessage(
      ausResult.ausResult1.code,
      ausResult.ausResult2.code,
      ausResult.ausResult3.code,
      ausResult.ausResult4.code,
      ausResult.ausResult5.code,
      ausResult.otherAusResult
    )

  def ausResultFromProtobuf(msg: AusResultMessage): AutomatedUnderwritingSystemResult = {
    val ausResult1 = AutomatedUnderwritingResultEnum.valueOf(msg.ausResult1)
    val ausResult2 = AutomatedUnderwritingResultEnum.valueOf(msg.ausResult2)
    val ausResult3 = AutomatedUnderwritingResultEnum.valueOf(msg.ausResult3)
    val ausResult4 = AutomatedUnderwritingResultEnum.valueOf(msg.ausResult4)
    val ausResult5 = AutomatedUnderwritingResultEnum.valueOf(msg.ausResult5)

    AutomatedUnderwritingSystemResult(ausResult1, ausResult2, ausResult3, ausResult4, ausResult5, msg.otherAusResult)
  }

  def loanApplicationRegisterToProtobuf(lar: LoanApplicationRegister): LoanApplicationRegisterMessage = {
    val larIdentifier         = larIdentifierToProtobuf(lar.larIdentifier)
    val loan                  = loanToProtobuf(lar.loan)
    val action                = larActionToProtobuf(lar.action)
    val geography             = geographyToProtobuf(lar.geography)
    val applicant             = applicantToProtobuf(lar.applicant)
    val coApplicant           = applicantToProtobuf(lar.coApplicant)
    val denial                = denialToProtobuf(lar.denial)
    val loanDisclosure        = loanDisclosureToProtobuf(lar.loanDisclosure)
    val nonAmortizingFeatures = nonAmortizingFeaturesToProtobuf(lar.nonAmortizingFeatures)
    val property              = propertyToProtobuf(lar.property)
    val AUS                   = ausToProtobuf(lar.AUS)
    val ausResult             = ausResultToProtobuf(lar.ausResult)

    LoanApplicationRegisterMessage(
      Some(larIdentifier),
      Some(loan),
      Some(action),
      Some(geography),
      Some(applicant),
      Some(coApplicant),
      lar.income,
      lar.purchaserType.code,
      lar.hoepaStatus.code,
      lar.lienStatus.code,
      Some(denial),
      Some(loanDisclosure),
      Some(nonAmortizingFeatures),
      Some(property),
      lar.applicationSubmission.code,
      lar.payableToInstitution.code,
      Some(AUS),
      Some(ausResult),
      lar.reverseMortgage.code,
      lar.lineOfCredit.code,
      lar.businessOrCommercialPurpose.code
    )
  }

  def loanApplicationRegisterFromProtobuf(msg: LoanApplicationRegisterMessage): LoanApplicationRegister = {
    val larIdentifier         = larIdentifierFromProtobuf(msg.larIdentifier.getOrElse(LarIdentifierMessage()))
    val loan                  = loanFromProtobuf(msg.loan.getOrElse(LoanMessage()))
    val action                = larActionFromProtobuf(msg.action.getOrElse(LarActionMessage()))
    val geography             = geographyFromProtobuf(msg.geography.getOrElse(GeographyMessage()))
    val applicant             = applicantFromProtobuf(msg.applicant.getOrElse(ApplicantMessage()))
    val coApplicant           = applicantFromProtobuf(msg.coApplicant.getOrElse(ApplicantMessage()))
    val denial                = denialFromProtobuf(msg.denial.getOrElse(DenialMessage()))
    val loanDisclosure        = loanDisclosureFromProtobuf(msg.loanDisclosure.getOrElse(LoanDisclosureMessage()))
    val nonAmortizingFeatures = nonAmortizingFeaturesFromProtobuf(msg.nonAmortizingFeatures.getOrElse(NonAmortizingFeaturesMessage()))
    val property              = propertyFromProtobuf(msg.property.getOrElse(PropertyMessage()))
    val AUS                   = ausFromProtobuf(msg.aUS.getOrElse(AusMessage()))
    val ausResult             = ausResultFromProtobuf(msg.ausResult.getOrElse(AusResultMessage()))

    val purchaserType = PurchaserEnum.valueOf(msg.purchaserType)
    val hoepaStatus   = HOEPAStatusEnum.valueOf(msg.hoepaStatus)
    val lienStatus    = LienStatusEnum.valueOf(msg.lienStatus)
    val applicationSubmission =
      ApplicationSubmissionEnum.valueOf(msg.applicationSubmission)
    val payableToInstitution =
      PayableToInstitutionEnum.valueOf(msg.payableToInstitution)
    val reverseMortgage = MortgageTypeEnum.valueOf(msg.reverseMortgage)
    val lineOfCredit    = LineOfCreditEnum.valueOf(msg.lineOfCredit)
    val businessOrCommercialPurpose =
      BusinessOrCommercialBusinessEnum.valueOf(msg.businessOrCommercialPurpose)

    LoanApplicationRegister(
      larIdentifier,
      loan,
      action,
      geography,
      applicant,
      coApplicant,
      msg.income,
      purchaserType,
      hoepaStatus,
      lienStatus,
      denial,
      loanDisclosure,
      nonAmortizingFeatures,
      property,
      applicationSubmission,
      payableToInstitution,
      AUS,
      ausResult,
      reverseMortgage,
      lineOfCredit,
      businessOrCommercialPurpose
    )
  }
}
