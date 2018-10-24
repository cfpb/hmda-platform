package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import hmda.model.filing.{HmdaFileRow, PipeDelimited}

case class LoanApplicationRegister(
    larIdentifier: LarIdentifier = LarIdentifier(),
    loan: Loan = Loan(),
    action: LarAction = LarAction(),
    geography: Geography = Geography(),
    applicant: Applicant = Applicant(),
    coApplicant: Applicant = Applicant(),
    income: String = "",
    purchaserType: PurchaserEnum = InvalidPurchaserCode,
    hoepaStatus: HOEPAStatusEnum = InvalidHoepaStatusCode,
    lienStatus: LienStatusEnum = InvalidLienStatusCode,
    denial: Denial = Denial(),
    loanDisclosure: LoanDisclosure = LoanDisclosure(),
    nonAmortizingFeatures: NonAmortizingFeatures = NonAmortizingFeatures(),
    property: Property = Property(),
    applicationSubmission: ApplicationSubmissionEnum =
      InvalidApplicationSubmissionCode,
    payableToInstitution: PayableToInstitutionEnum =
      InvalidPayableToInstitutionCode,
    AUS: AutomatedUnderwritingSystem = AutomatedUnderwritingSystem(),
    ausResult: AutomatedUnderwritingSystemResult =
      AutomatedUnderwritingSystemResult(),
    reverseMortgage: MortgageTypeEnum = InvalidMortgageTypeCode,
    lineOfCredit: LineOfCreditEnum = InvalidLineOfCreditCode,
    businessOrCommercialPurpose: BusinessOrCommercialBusinessEnum =
      InvalidBusinessOrCommercialBusinessCode
) extends PipeDelimited
    with HmdaFileRow {

  override def toCSV: String = {

    s"${larIdentifier.id}|${larIdentifier.LEI}|${loan.ULI}|${loan.applicationDate}|${loan.loanType.code}|${loan.loanPurpose.code}|${action.preapproval.code}|" +
      s"${loan.constructionMethod.code}|${loan.occupancy.code}|${loan.amount}|${action.actionTakenType.code}|${action.actionTakenDate}|" +
      s"${geography.toCSV}|" +
      s"${applicant.ethnicity.ethnicity1.code}|${applicant.ethnicity.ethnicity2.code}|${applicant.ethnicity.ethnicity3.code}|" +
      s"${applicant.ethnicity.ethnicity4.code}|${applicant.ethnicity.ethnicity5.code}|${applicant.ethnicity.otherHispanicOrLatino}|" +
      s"${coApplicant.ethnicity.ethnicity1.code}|${coApplicant.ethnicity.ethnicity2.code}|${coApplicant.ethnicity.ethnicity3.code}|" +
      s"${coApplicant.ethnicity.ethnicity4.code}|${coApplicant.ethnicity.ethnicity5.code}|${coApplicant.ethnicity.otherHispanicOrLatino}|" +
      s"${applicant.ethnicity.ethnicityObserved.code}|${coApplicant.ethnicity.ethnicityObserved.code}|${applicant.race.race1.code}|" +
      s"${applicant.race.race2.code}|${applicant.race.race3.code}|${applicant.race.race4.code}|${applicant.race.race5.code}|${applicant.race.otherNativeRace}|" +
      s"${applicant.race.otherAsianRace}|${applicant.race.otherPacificIslanderRace}|${coApplicant.race.race1.code}|${coApplicant.race.race2.code}|" +
      s"${coApplicant.race.race3.code}|${coApplicant.race.race4.code}|${coApplicant.race.race5.code}|${coApplicant.race.otherNativeRace}|${coApplicant.race.otherAsianRace}|" +
      s"${coApplicant.race.otherPacificIslanderRace}|${applicant.race.raceObserved.code}|${coApplicant.race.raceObserved.code}|" +
      s"${applicant.sex.sexEnum.code}|${coApplicant.sex.sexEnum.code}|${applicant.sex.sexObservedEnum.code}|${coApplicant.sex.sexObservedEnum.code}|" +
      s"${applicant.age}|${coApplicant.age}|$income|${purchaserType.code}|${loan.rateSpread}|${hoepaStatus.code}|${lienStatus.code}|${applicant.creditScore}|${coApplicant.creditScore}|" +
      s"${applicant.creditScoreType.code}|${applicant.otherCreditScoreModel}|${coApplicant.creditScoreType.code}|${coApplicant.otherCreditScoreModel}|" +
      s"${denial.denialReason1.code}|${denial.denialReason2.code}|${denial.denialReason3.code}|${denial.denialReason4.code}|${denial.otherDenialReason}|${loanDisclosure.totalLoanCosts}|" +
      s"${loanDisclosure.totalPointsAndFees}|${loanDisclosure.originationCharges}|${loanDisclosure.discountPoints}|${loanDisclosure.lenderCredits}|${loan.interestRate}|" +
      s"${loan.prepaymentPenaltyTerm}|${loan.debtToIncomeRatio}|${loan.combinedLoanToValueRatio}|${loan.loanTerm}|${loan.introductoryRatePeriod}|${nonAmortizingFeatures.balloonPayment.code}|" +
      s"${nonAmortizingFeatures.interestOnlyPayments.code}|${nonAmortizingFeatures.negativeAmortization.code}|${nonAmortizingFeatures.otherNonAmortizingFeatures.code}|" +
      s"${property.propertyValue}|${property.manufacturedHomeSecuredProperty.code}|${property.manufacturedHomeLandPropertyInterest.code}|${property.totalUnits}|${property.multiFamilyAffordableUnits}|${applicationSubmission.code}|" +
      s"${payableToInstitution.code}|${larIdentifier.NMLSRIdentifier}|${AUS.toCSV}|${ausResult.toCSV}|${reverseMortgage.code}|${lineOfCredit.code}|${businessOrCommercialPurpose.code}"

  }

  override def valueOf(field: String): Any = {
    LarFieldMapping.mapping(this).getOrElse(field, "error: field name mismatch")
  }
}
