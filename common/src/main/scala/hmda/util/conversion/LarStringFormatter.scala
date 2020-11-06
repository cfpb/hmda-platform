package hmda.util.conversion

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.modifiedlar.EnrichedModifiedLoanApplicationRegister

object LarStringFormatter {

  private def convertEmptyField(code: Int) =
    if (code == 0) "" else code.toString

  def larString(lar: LoanApplicationRegister): String =
      lar.larIdentifier.LEI +
      lar.loan.ULI +
//    lar.loan.applicationDate +
      lar.loan.loanType.code +
      lar.loan.loanPurpose.code +
      lar.action.preapproval.code +
      lar.loan.constructionMethod.code +
      lar.loan.occupancy.code +
      lar.action.actionTakenType.code +
      lar.action.actionTakenDate +
//      lar.geography.street +
//      lar.geography.city +
//      lar.geography.state +
//      lar.geography.zipCode +
//      lar.geography.county +
//      lar.geography.tract +
      convertEmptyField(lar.applicant.ethnicity.ethnicity1.code) +
      convertEmptyField(lar.applicant.ethnicity.ethnicity2.code) +
      convertEmptyField(lar.applicant.ethnicity.ethnicity3.code) +
      convertEmptyField(lar.applicant.ethnicity.ethnicity4.code) +
      convertEmptyField(lar.applicant.ethnicity.ethnicity5.code) +
//      lar.applicant.ethnicity.otherHispanicOrLatino +
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity1.code) +
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity2.code) +
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity3.code) +
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity4.code) +
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity5.code) +
//      lar.coApplicant.ethnicity.otherHispanicOrLatino +
      lar.applicant.ethnicity.ethnicityObserved.code +
      lar.coApplicant.ethnicity.ethnicityObserved.code +
      convertEmptyField(lar.applicant.race.race1.code) +
      convertEmptyField(lar.applicant.race.race2.code) +
      convertEmptyField(lar.applicant.race.race3.code) +
      convertEmptyField(lar.applicant.race.race4.code) +
      convertEmptyField(lar.applicant.race.race5.code) +
//      lar.applicant.race.otherNativeRace +
//      lar.applicant.race.otherAsianRace +
//      lar.applicant.race.otherPacificIslanderRace +
      convertEmptyField(lar.coApplicant.race.race1.code) +
      convertEmptyField(lar.coApplicant.race.race2.code) +
      convertEmptyField(lar.coApplicant.race.race3.code) +
      convertEmptyField(lar.coApplicant.race.race4.code) +
      convertEmptyField(lar.coApplicant.race.race5.code) +
//      lar.coApplicant.race.otherNativeRace +
//      lar.coApplicant.race.otherAsianRace +
//      lar.coApplicant.race.otherPacificIslanderRace +
      lar.applicant.race.raceObserved.code +
      lar.coApplicant.race.raceObserved.code +
      lar.applicant.sex.sexEnum.code +
      lar.coApplicant.sex.sexEnum.code +
      lar.applicant.sex.sexObservedEnum.code +
      lar.coApplicant.sex.sexObservedEnum.code +
//      lar.applicant.age +
//      lar.coApplicant.age +
      lar.income +
      lar.purchaserType.code +
      lar.loan.rateSpread +
      lar.hoepaStatus.code +
      lar.lienStatus.code +
//      lar.applicant.creditScore +
//      lar.coApplicant.creditScore +
//      lar.applicant.creditScoreType.code +
//      lar.applicant.otherCreditScoreModel +
//      lar.coApplicant.creditScoreType.code +
//      lar.coApplicant.otherCreditScoreModel //+
      convertEmptyField(lar.denial.denialReason1.code) +
      convertEmptyField(lar.denial.denialReason2.code) +
      convertEmptyField(lar.denial.denialReason3.code) +
      convertEmptyField(lar.denial.denialReason4.code) +
//      lar.denial.otherDenialReason +
      lar.loanDisclosure.totalLoanCosts +
      lar.loanDisclosure.totalPointsAndFees +
      lar.loanDisclosure.originationCharges +
      lar.loanDisclosure.discountPoints +
      lar.loanDisclosure.lenderCredits +
      lar.loan.interestRate +
      lar.loan.prepaymentPenaltyTerm +
      lar.loan.debtToIncomeRatio +
      lar.loan.combinedLoanToValueRatio +
      lar.loan.loanTerm +
      lar.loan.introductoryRatePeriod +
      lar.nonAmortizingFeatures.balloonPayment.code +
      lar.nonAmortizingFeatures.interestOnlyPayments.code +
      lar.nonAmortizingFeatures.negativeAmortization.code +
      lar.nonAmortizingFeatures.otherNonAmortizingFeatures.code +
////      lar.property.propertyValue // +
////      lar.property.manufacturedHomeSecuredProperty.code +
////      lar.property.manufacturedHomeLandPropertyInterest.code +
      lar.property.totalUnits +
      lar.property.multiFamilyAffordableUnits +
      lar.applicationSubmission.code +
      lar.payableToInstitution.code +
////      lar.larIdentifier.NMLSRIdentifier +
////      convertEmptyField(lar.AUS.aus1.code) +
////      convertEmptyField(lar.AUS.aus2.code) +
////      convertEmptyField(lar.AUS.aus3.code) +
////      convertEmptyField(lar.AUS.aus4.code) +
////      convertEmptyField(lar.AUS.aus5.code) +
////      lar.AUS.otherAUS +
////      lar.ausResult.ausResult1.code +
////      convertEmptyField(lar.ausResult.ausResult2.code) +
////      convertEmptyField(lar.ausResult.ausResult3.code) +
////      convertEmptyField(lar.ausResult.ausResult4.code) +
////      convertEmptyField(lar.ausResult.ausResult5.code) +
////      lar.ausResult.otherAusResult +
      lar.reverseMortgage.code +
      lar.lineOfCredit.code +
      lar.businessOrCommercialPurpose.code

  def larString(emlar: EnrichedModifiedLoanApplicationRegister): String =
    emlar.mlar.lei +
    emlar.mlar.uli +
    emlar.mlar.loanType +
    emlar.mlar.loanPurpose +
    emlar.mlar.preapproval +
    emlar.mlar.constructionMethod +
    emlar.mlar.occupancy +
    emlar.mlar.actionTakenType +
    emlar.mlar.actionTakenDate +
    emlar.mlar.ethnicity1 +
    emlar.mlar.ethnicity2 +
    emlar.mlar.ethnicity3 +
    emlar.mlar.ethnicity4 +
    emlar.mlar.ethnicity5 +
    emlar.mlar.coEthnicity1 +
    emlar.mlar.coEthnicity2 +
    emlar.mlar.coEthnicity3 +
    emlar.mlar.coEthnicity4 +
    emlar.mlar.coEthnicity5 +
    emlar.mlar.ethnicityVisualObservation +
    emlar.mlar.coEthnicityVisualObservation +
    emlar.mlar.race1 +
    emlar.mlar.race2 +
    emlar.mlar.race3 +
    emlar.mlar.race4 +
    emlar.mlar.race5 +
    emlar.mlar.coRace1 +
    emlar.mlar.coRace2 +
    emlar.mlar.coRace3 +
    emlar.mlar.coRace4 +
    emlar.mlar.coRace5 +
    emlar.mlar.raceVisualObservation +
    emlar.mlar.coRaceVisualObservation +
    emlar.mlar.sex +
    emlar.mlar.coSex +
    emlar.mlar.sexVisualObservation +
    emlar.mlar.coSexVisualObservation +
    emlar.mlar.income +
    emlar.mlar.purchaserType +
    emlar.mlar.rateSpread +
    emlar.mlar.hoepaStatus +
    emlar.mlar.lienStatus +
      emlar.mlar.denial1 +
      emlar.mlar.denial2 +
      emlar.mlar.denial3 +
      emlar.mlar.denial4 +
      emlar.mlar.totalLoanCosts +
      emlar.mlar.totalPointsAndFees +
      emlar.mlar.originationCharges +
      emlar.mlar.discountPoints +
      emlar.mlar.lenderCredits +
      emlar.mlar.interestRate +
      emlar.mlar.prepaymentPenalty +
  emlar.mlar.debtToIncomeRatio +
      emlar.mlar.loanToValueRatio +
      emlar.mlar.loanTerm +
      emlar.mlar.introductoryRatePeriod +
      emlar.mlar.balloonPayment +
      emlar.mlar.interestOnlyPayment +
      emlar.mlar.negativeAmortization +
      emlar.mlar.otherNonAmortizingFeatures +
  emlar.mlar.totalUnits +
      emlar.mlar.multifamilyAffordableUnits +
      emlar.mlar.applicationSubmission +
      emlar.mlar.initiallyPayableToInstitution +
  emlar.mlar.reverseMortgage +
      emlar.mlar.openEndLineOfCredit +
      emlar.mlar.businessOrCommercialPurpose +


}
