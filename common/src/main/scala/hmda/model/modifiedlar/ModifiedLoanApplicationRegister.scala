package hmda.model.modifiedlar

import hmda.model.census.Census
import hmda.model.filing.PipeDelimited
import hmda.util.conversion.ColumnDataFormatter

// ModifiedLoanApplicationRegister enriched with tract Census information
case class EnrichedModifiedLoanApplicationRegister(mlar: ModifiedLoanApplicationRegister, census: Census)

case class ModifiedLoanApplicationRegister(
                                            id: Int,
                                            year: Int,
                                            uli: String, // Note: this must not be in toCSV method
                                            lei: String,
                                            loanType: Int,
                                            loanPurpose: Int,
                                            preapproval: Int,
                                            constructionMethod: Int,
                                            occupancy: Int,
                                            loanAmount: String,
                                            actionTakenType: Int,
                                            actionTakenDate: Int,
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
                                            dwellingCategorization: String,
                                            loanProductTypeCategorization: String
                                          ) extends PipeDelimited
  with ColumnDataFormatter {
  override def toCSV: String =
    (s"$year|$lei|$loanType|$loanPurpose|$preapproval|$constructionMethod|$occupancy|" +
      loanAmount +
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
      controlCharacterFilter(s"$denial1|$denial2|$denial3|$denial4") +
      s"|$totalLoanCosts|$totalPointsAndFees|$originationCharges|$discountPoints|$lenderCredits|$interestRate|" +
      s"$prepaymentPenalty|$debtToIncomeRatio|$loanToValueRatio|$loanTerm|" +
      s"$introductoryRatePeriod|$balloonPayment|$interestOnlyPayment|$negativeAmortization|$otherNonAmortizingFeatures|" +
      toBigDecimalString(propertyValue) +
      s"|" +
      s"$homeSecuredPropertyType|$homeLandPropertyType|$totalUnits|$multifamilyAffordableUnits|$applicationSubmission|" +
      s"$initiallyPayableToInstitution|$AUS1|$AUS2|$AUS3|$AUS4|$AUS5|" +
      s"$reverseMortgage|$openEndLineOfCredit|$businessOrCommercialPurpose")
      .replaceAll("(\r\n)|\r|\n", "")

}

object ModifiedLoanApplicationRegister {
  def header: String = """activity_year|lei|loan_type|loan_purpose|preapproval|construction_method|occupancy_type|loan_amount|action_taken|state_code|county_code|census_tract|applicant_ethnicity_1|applicant_ethnicity_2|applicant_ethnicity_3|applicant_ethnicity_4|applicant_ethnicity_5|co_applicant_ethnicity_1|co_applicant_ethnicity_2|co_applicant_ethnicity_3|co_applicant_ethnicity_4|co_applicant_ethnicity_5|applicant_ethnicity_observed|co_applicant_ethnicity_observed|applicant_race_1|applicant_race_2|applicant_race_3|applicant_race_4|applicant_race_5|co_applicant_race_1|co_applicant_race_2|co_applicant_race_3|co_applicant_race_4|co_applicant_race_5|applicant_race_observed|co_applicant_race_observed|applicant_sex|co_applicant_sex|applicant_sex_observed|co_applicant_sex_observed|applicant_age|applicant_age_above_62|co_applicant_age|co_applicant_age_above_62|income|purchaser_type|rate_spread|hoepa_status|lien_status|applicant_credit_scoring_model|co_applicant_credit_scoring_model|denial_reason_1|denial_reason_2|denial_reason_3|denial_reason_4|total_loan_costs|total_points_and_fees|origination_charges|discount_points|lender_credits|interest_rate|prepayment_penalty_term|debt_to_income_ratio|combined_loan_to_value_ratio|loan_term|intro_rate_period|balloon_payment|interest_only_payment|negative_amortization|other_non_amortizing_features|property_value|manufactured_home_secured_property_type|manufactured_home_land_property_interest|total_units|multifamily_affordable_units|submission_of_application|initially_payable_to_institution|aus_1|aus_2|aus_3|aus_4|aus_5|reverse_mortgage|open_end_line_of_credit|business_or_commercial_purpose"""+"\n"
}

