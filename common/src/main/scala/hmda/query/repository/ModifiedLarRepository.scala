package hmda.query.repository

import java.security.MessageDigest

import hmda.model.filing.submission.SubmissionId
import hmda.model.modifiedlar.EnrichedModifiedLoanApplicationRegister
import hmda.util.conversion.LarStringFormatter
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.util.Try

class ModifiedLarRepository(databaseConfig: DatabaseConfig[JdbcProfile]) {
  import databaseConfig.profile.api._

  private val db = databaseConfig.db

  def fetchYearTable(year: Int): String =
    year match {
      case 2018 => "modifiedlar2018"
      case 2019 => "modifiedlar2019"
      case 2020 => "modifiedlar2020"
      case 2021 => "modifiedlar2021"
      case 2022 => "modifiedlar2022"
      case 2023 => "modifiedlar2023"
      case 2024 => "modifiedlar2024"
      case 2025 => "modifiedlar2025"
      case _    => "modifiedlar2024"
    }

  /**
    * Deletes entries in the Modified LAR table by their LEI
    * @param lei
    * @return the number of rows removed
    */
  def msaMds(lei: String, filingYear: Int): Future[Vector[(String, String)]] =
    db.run {
      sql"""SELECT  msa_md, case when msa_md = '99999' then 'NA' else max(msa_md_name) end
                         FROM #${fetchYearTable(filingYear)} WHERE lei = ${lei.toUpperCase}  AND msa_md <> 0 group by msa_md order by msa_md"""
        .as[(String, String)]
    }

  /**
    * Deletes entries in the Modified LAR table by their LEI
    * @param submissionId
    * @return the number of rows removed
    */
  def deleteByLei(submissionId: SubmissionId): Future[Int] =
    db.run(
      sqlu"DELETE FROM #${fetchYearTable(submissionId.period.year.toInt)} WHERE UPPER(lei) = ${submissionId.lei.toUpperCase} and filing_year = ${submissionId.period.year.toInt}"
    )

  /**
    * Inserts Modified Loan Application Register data that has been enhanced with Census information via the tract map
    * @param input
    * @param submissionId
    * @return
    */
  def insert(input: EnrichedModifiedLoanApplicationRegister, submissionId: SubmissionId): Future[Int] =
    db.run(sqlu"""INSERT INTO #${fetchYearTable(submissionId.period.year.toInt)} (
            id,
            lei,
            loan_type,
            loan_purpose,
            preapproval,
            construction_method,
            occupancy_type,
            loan_amount,
            action_taken_type,
            action_taken_date,
            state,
            county,
            tract,
            ethnicity_applicant_1,
            ethnicity_applicant_2,
            ethnicity_applicant_3,
            ethnicity_applicant_4,
            ethnicity_applicant_5,
            ethnicity_observed_applicant,
            ethnicity_co_applicant_1,
            ethnicity_co_applicant_2,
            ethnicity_co_applicant_3,
            ethnicity_co_applicant_4,
            ethnicity_co_applicant_5,
            ethnicity_observed_co_applicant,
            race_applicant_1,
            race_applicant_2,
            race_applicant_3,
            race_applicant_4,
            race_applicant_5,
            race_co_applicant_1,
            race_co_applicant_2,
            race_co_applicant_3,
            race_co_applicant_4,
            race_co_applicant_5,
            race_observed_applicant,
            race_observed_co_applicant,
            sex_applicant,
            sex_co_applicant,
            observed_sex_applicant,
            observed_sex_co_applicant,
            age_applicant,
            applicant_age_greater_than_62,
            age_co_applicant,
            coapplicant_age_greater_than_62,
            income,
            purchaser_type,
            rate_spread,
            hoepa_status,
            lien_status,
            credit_score_type_applicant,
            credit_score_type_co_applicant,
            denial_reason1,
            denial_reason2,
            denial_reason3,
            denial_reason4,
            total_loan_costs,
            total_points,
            origination_charges,
            discount_points,
            lender_credits,
            interest_rate,
            payment_penalty,
            debt_to_incode,
            loan_value_ratio,
            loan_term,
            rate_spread_intro,
            baloon_payment,
            insert_only_payment,
            amortization,
            other_amortization,
            property_value,
            home_security_policy,
            lan_property_interest,
            total_units,
            mf_affordable,
            application_submission,
            payable,
            aus1,
            aus2,
            aus3,
            aus4,
            aus5,
            reverse_mortgage,
            line_of_credits,
            business_or_commercial,
            population,
            minority_population_percent,
            ffiec_med_fam_income,
            tract_to_msamd,
            owner_occupied_units,
            one_to_four_fam_units,
            msa_md,
            msa_md_name,
            submission_id,
            filing_year,
            conforming_loan_limit,
            median_age,
            median_age_calculated,
            median_income_percentage,
            ethnicity_categorization,
            race_categorization,
            sex_categorization,
            percent_median_msa_income,
            dwelling_category,
            loan_product_type,
            uli,
            checksum
            )


          VALUES (
            ${input.mlar.id},
            ${input.mlar.lei},
            ${input.mlar.loanType},
            ${input.mlar.loanPurpose},
            ${input.mlar.preapproval},
            ${input.mlar.constructionMethod},
            ${input.mlar.occupancy},
            ${input.mlar.loanAmount},
            ${input.mlar.actionTakenType},
            ${input.mlar.actionTakenDate},
            ${input.mlar.state},
            ${input.mlar.county},
            ${input.mlar.tract},
            ${safeConvertToInt(input.mlar.ethnicity1)},
            ${safeConvertToInt(input.mlar.ethnicity2)},
            ${safeConvertToInt(input.mlar.ethnicity3)},
            ${safeConvertToInt(input.mlar.ethnicity4)},
            ${safeConvertToInt(input.mlar.ethnicity5)},
            ${input.mlar.ethnicityVisualObservation},
            ${safeConvertToInt(input.mlar.coEthnicity1)},
            ${safeConvertToInt(input.mlar.coEthnicity2)},
            ${safeConvertToInt(input.mlar.coEthnicity3)},
            ${safeConvertToInt(input.mlar.coEthnicity4)},
            ${safeConvertToInt(input.mlar.coEthnicity5)},
            ${input.mlar.coEthnicityVisualObservation},
            ${safeConvertToInt(input.mlar.race1)},
            ${safeConvertToInt(input.mlar.race2)},
            ${safeConvertToInt(input.mlar.race3)},
            ${safeConvertToInt(input.mlar.race4)},
            ${safeConvertToInt(input.mlar.race5)},
            ${safeConvertToInt(input.mlar.coRace1)},
            ${safeConvertToInt(input.mlar.coRace2)},
            ${safeConvertToInt(input.mlar.coRace3)},
            ${safeConvertToInt(input.mlar.coRace4)},
            ${safeConvertToInt(input.mlar.coRace5)},
            ${input.mlar.raceVisualObservation},
            ${input.mlar.coRaceVisualObservation},
            ${input.mlar.sex},
            ${input.mlar.coSex},
            ${input.mlar.sexVisualObservation},
            ${input.mlar.coSexVisualObservation},
            ${input.mlar.age},
            ${input.mlar.ageGreaterThanOrEqual62},
            ${input.mlar.coAge},
            ${input.mlar.coAgeGreaterThanOrEqual62},
            ${input.mlar.income},
            ${input.mlar.purchaserType},
            ${input.mlar.rateSpread},
            ${input.mlar.hoepaStatus},
            ${input.mlar.lienStatus},
            ${input.mlar.applicantCredisScoreModel},
            ${input.mlar.coApplicantCreditScoreModel},
            ${safeConvertToInt(input.mlar.denial1)},
            ${safeConvertToInt(input.mlar.denial2)},
            ${safeConvertToInt(input.mlar.denial3)},
            ${safeConvertToInt(input.mlar.denial4)},
            ${input.mlar.totalLoanCosts},
            ${input.mlar.totalPointsAndFees},
            ${input.mlar.originationCharges},
            ${input.mlar.discountPoints},
            ${input.mlar.lenderCredits},
            ${input.mlar.interestRate},
            ${input.mlar.prepaymentPenalty},
            ${input.mlar.debtToIncomeRatio},
            ${input.mlar.loanToValueRatio},
            ${input.mlar.loanTerm},
            ${input.mlar.introductoryRatePeriod},
            ${input.mlar.balloonPayment},
            ${input.mlar.interestOnlyPayment},
            ${input.mlar.negativeAmortization},
            ${input.mlar.otherNonAmortizingFeatures},
            ${input.mlar.propertyValue},
            ${input.mlar.homeSecuredPropertyType},
            ${input.mlar.homeLandPropertyType},
            ${input.mlar.totalUnits},
            ${input.mlar.multifamilyAffordableUnits},
            ${input.mlar.applicationSubmission},
            ${input.mlar.initiallyPayableToInstitution},
            ${safeConvertToInt(input.mlar.AUS1)},
            ${safeConvertToInt(input.mlar.AUS2)},
            ${safeConvertToInt(input.mlar.AUS3)},
            ${safeConvertToInt(input.mlar.AUS4)},
            ${safeConvertToInt(input.mlar.AUS5)},
            ${input.mlar.reverseMortgage},
            ${input.mlar.openEndLineOfCredit},
            ${input.mlar.businessOrCommercialPurpose},
            ${input.census.population},
            ${input.census.minorityPopulationPercent},
            ${input.census.medianIncome},
            ${input.census.tract},
            ${input.census.occupiedUnits},
            ${input.census.oneToFourFamilyUnits},
            ${input.census.msaMd},
            ${input.census.name},
            ${submissionId.toString},
            ${submissionId.period.year.toInt},
            ${input.mlar.conformingLoanLimit},
            ${input.census.medianAge},
            ${medianAgeCalculated(submissionId.period.year.toInt, input.census.medianAge)},
            ${input.census.tracttoMsaIncomePercent},
            ${input.mlar.ethnicityCategorization},
            ${input.mlar.raceCategorization},
            ${input.mlar.sexCategorization},
            ${incomeCategorization(input.mlar.income, input.census.medianIncome)},
            ${input.mlar.dwellingCategorization},
            ${input.mlar.loanProductTypeCategorization},
            ${input.mlar.uli},
            ${checksum_calc(input)}
          )
          """)

  private def checksum_calc(input: EnrichedModifiedLoanApplicationRegister): String =
    MessageDigest.getInstance("MD5")
      .digest(LarStringFormatter.larString(input).toUpperCase().getBytes())
      .map(0xFF & _)
      .map { "%02x".format(_) }.foldLeft(""){_ + _}

  private def safeConvertToInt(s: String): Option[Int] =
    Try(s.toInt).toOption

  def incomeCategorization(larIncome: String, censusMedianIncome: Int): String =
    if (larIncome == "NA" ||larIncome == "")
      "NA"
    else {
      //income in the lar is rounded to 1000
      val income    = larIncome.toDouble * 1000
      val fifty     = censusMedianIncome * .5
      val eighty    = censusMedianIncome * .8
      val oneTwenty = censusMedianIncome * 1.2

      if (income < fifty) {
        "<50%"
      } else if (income >= fifty && income < eighty) {
        "50-79%"
      } else if (income >= eighty && income < censusMedianIncome) {
        "80-99%"
      } else if (income >= censusMedianIncome && income < oneTwenty) {
        "100-119%"
      } else {
        ">120%"
      }
    }

  def medianAgeCalculated(filingYear: Int, medianAge: Int): String = {
    val medianYear = filingYear - medianAge
    if (medianAge == -1)
      "Age Unknown"
    else if (medianYear <= 1969)
      "1969 or Earlier"
    else if (medianYear >= 1970 && medianYear <= 1979)
      "1970 - 1979"
    else if (medianYear >= 1980 && medianYear <= 1989)
      "1980 - 1989"
    else if (medianYear >= 1990 && medianYear <= 1999)
      "1990 - 1999"
    else if (medianYear >= 2000 && medianYear <= 2010)
      "2000 - 2010"
    else if (medianYear >= 2011)
      "2011 - Present"
    else
      "Age Unknown"
  }

}
