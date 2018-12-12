package hmda.publication.lar.parser

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.publication.lar.model.ModifiedLoanApplicationRegister

object ModifiedLarCsvParser {

  def apply(s: String): ModifiedLoanApplicationRegister = {
    convert(parseLar(s))
  }

  //TODO: Parse LAR
  private def parseLar(s: String): LoanApplicationRegister = ???

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
      lar.loan.amount.toInt,
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
      lar.applicant.age.toString,
      "",
      lar.coApplicant.age.toString,
      "",
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
      lar.property.propertyValue,
      lar.property.manufacturedHomeSecuredProperty.code,
      lar.property.manufacturedHomeLandPropertyInterest.code,
      lar.property.totalUnits.toString,
      lar.property.multiFamilyAffordableUnits,
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

}
