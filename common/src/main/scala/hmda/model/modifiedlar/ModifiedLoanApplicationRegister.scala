package hmda.model.modifiedlar

import hmda.model.census.Census
import hmda.model.filing.PipeDelimited
import hmda.util.conversion.ColumnDataFormatter

// ModifiedLoanApplicationRegister enriched with tract Census information
case class EnrichedModifiedLoanApplicationRegister(
    mlar: ModifiedLoanApplicationRegister,
    census: Census)

case class ModifiedLoanApplicationRegister(
    id: Int,
    lei: String,
    loanType: Int,
    loanPurpose: Int,
    preapproval: Int,
    constructionMethod: Int,
    occupancy: Int,
    loanAmount: Int,
    actionTakenType: Int,
    state: String,
    county: String,
    tract: String,
    ethnicity1: String,
    ethnicity2: String,
    ethnicity3: String,
    ethnicity4: String,
    ethnicity5: String,
    ethnicityVisualObservation: Int,
    coEthnicity1: String,
    coEthnicity2: String,
    coEthnicity3: String,
    coEthnicity4: String,
    coEthnicity5: String,
    coEthnicityVisualObservation: Int,
    race1: String,
    race2: String,
    race3: String,
    race4: String,
    race5: String,
    coRace1: String,
    coRace2: String,
    coRace3: String,
    coRace4: String,
    coRace5: String,
    raceVisualObservation: Int,
    coRaceVisualObservation: Int,
    sex: Int,
    coSex: Int,
    sexVisualObservation: Int,
    coSexVisualObservation: Int,
    age: String,
    ageGreaterThanOrEqual62: String,
    coAge: String,
    coAgeGreaterThanOrEqual62: String,
    income: String,
    purchaserType: Int,
    rateSpread: String,
    hoepaStatus: Int,
    lienStatus: Int,
    applicantCredisScoreModel: Int,
    coApplicantCreditScoreModel: Int,
    denial1: String,
    denial2: String,
    denial3: String,
    denial4: String,
    totalLoanCosts: String,
    totalPointsAndFees: String,
    originationCharges: String,
    discountPoints: String,
    lenderCredits: String,
    interestRate: String,
    prepaymentPenalty: String,
    debtToIncomeRatio: String,
    loanToValueRatio: String,
    loanTerm: String,
    introductoryRatePeriod: String,
    balloonPayment: Int,
    interestOnlyPayment: Int,
    negativeAmortization: Int,
    otherNonAmortizingFeatures: Int,
    propertyValue: String,
    homeSecuredPropertyType: Int,
    homeLandPropertyType: Int,
    totalUnits: String,
    multifamilyAffordableUnits: String,
    applicationSubmission: Int,
    initiallyPayableToInstitution: Int,
    AUS1: String,
    AUS2: String,
    AUS3: String,
    AUS4: String,
    AUS5: String,
    reverseMortgage: Int,
    openEndLineOfCredit: Int,
    businessOrCommercialPurpose: Int,
    conformingLoanLimit: String,
    ethnicityCategorization: String,
    raceCategorization: String,
    sexCategorization: String,

) extends PipeDelimited {

  override def toCSV: String = {
    (s"$id|$lei|$loanType|$loanPurpose|$preapproval|$constructionMethod|$occupancy|" +
      BigDecimal.valueOf(loanAmount).bigDecimal.toPlainString +
      s"|" +
      s"$actionTakenType|$state|$county|$tract|$ethnicity1|" +
      s"$ethnicity2|" +
      s"$ethnicity3|$ethnicity4|$ethnicity5|" +
      s"$coEthnicity1|$coEthnicity2|$coEthnicity3|" +
      s"$coEthnicity4|$coEthnicity5|$ethnicityVisualObservation|" +
      s"$coEthnicityVisualObservation|$race1|$race2|" +
      s"$race3|$race4|$race5|" +
      s"$coRace1|$coRace2|$coRace3|$coRace4|$coRace5|" +
      s"$raceVisualObservation|$coRaceVisualObservation|$sex|$coSex|" +
      s"$sexVisualObservation|$coSexVisualObservation|" +
      s"$age|$ageGreaterThanOrEqual62|$coAge|$coAgeGreaterThanOrEqual62|$income|" +
      s"$purchaserType|$rateSpread|$hoepaStatus|" +
      s"$lienStatus|$applicantCredisScoreModel|$coApplicantCreditScoreModel|" +
      ColumnDataFormatter.controlCharacterFilter(
        s"$denial1|$denial2|$denial3|$denial4") +
      s"|$totalLoanCosts|$totalPointsAndFees|$originationCharges|$discountPoints|$lenderCredits|$interestRate|" +
      s"$prepaymentPenalty|$debtToIncomeRatio|$loanToValueRatio|$loanTerm|" +
      s"$introductoryRatePeriod|$balloonPayment|$interestOnlyPayment|$negativeAmortization|$otherNonAmortizingFeatures|" +
      ColumnDataFormatter.toBigDecimalString(propertyValue) +
      s"|" +
      s"$homeSecuredPropertyType|$homeLandPropertyType|$totalUnits|$multifamilyAffordableUnits|$applicationSubmission|" +
      s"$initiallyPayableToInstitution|$AUS1|$AUS2|$AUS3|$AUS4|$AUS5|" +
      s"$reverseMortgage|$openEndLineOfCredit|$businessOrCommercialPurpose|$conformingLoanLimit")
      .replaceAll("(\r\n)|\r|\n", "")

  }

}
