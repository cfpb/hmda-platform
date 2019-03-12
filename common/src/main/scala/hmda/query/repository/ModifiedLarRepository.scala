package hmda.query.repository

import hmda.model.modifiedlar.EnrichedModifiedLoanApplicationRegister
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.util.Try

class ModifiedLarRepository(tableName: String,
                            databaseConfig: DatabaseConfig[JdbcProfile]) {
  import databaseConfig.profile.api._

  private val db = databaseConfig.db

  /**
    * FHA, FSA/RHS & VA (A): Total Units = 1 through 4; Purpose of Loan = 1; Loan Type = 2, 3, 4
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionATable1 (lei: String, msaMd: Int, tractToMsaMd: String): Unit = {
    db.run {
      sql"""select count(*), sum(loan_amount) from modifiedlar2018
           where UPPER(lei) = ${lei.toUpperCase}
           and action_taken_type = '1'
           and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
           and loan_purpose = 1
           and (loan_type = 2 or loan_type = 3 or loan_type = 4)
           and msa_md = ${msaMd}
           and tract_to_msamd = ${tractToMsaMd}
           group by lei"""
        .as[(Int, Int)]
    }
  }

  /**
    * Conventional (B): Total Units = 1 through 4; Purpose of Loan = 1; Loan Type = 1
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionBTable1 (lei: String, msaMd: Int, tractToMsaMd: String): Unit = {
    db.run {
      sql"""select count(*), sum(loan_amount) from modifiedlar2018
           where lei = ${lei.toUpperCase}
           and action_taken_type = '1'
           and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
           and loan_purpose = 1
           and loan_type = 1
           and msa_md = ${msaMd}
           and tract_to_msamd = ${tractToMsaMd}
           group by lei"""
        .as[(Int, Int)]
    }
  }

  /**
    * Refinancings (C): Total Units = 1 through 4; Purpose of Loan = 31, 32
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionCTable1 (lei: String, msaMd: Int, tractToMsaMd: String): Unit = {
    db.run {
      sql"""select count(*), sum(loan_amount) from modifiedlar2018
           where lei = ${lei.toUpperCase}
           and action_taken_type = '1'
           and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
           and (loan_purpose = 31 or loan_purpose = 32)
           and msa_md = ${msaMd}
           and tract_to_msamd = ${tractToMsaMd}
           group by lei"""
        .as[(Int, Int)]
    }
  }

  /**
    * Home Improvement Loans (D): Total Units = 1 through 4; Purpose of Loan = 2
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionDTable1 (lei: String, msaMd: Int, tractToMsaMd: String): Unit = {
    db.run {
      sql"""select count(*), sum(loan_amount) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and action_taken_type = '1'
            and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
            and loan_purpose = 2
            and msa_md = ${msaMd}
            and tract_to_msamd = ${tractToMsaMd}
            group by lei"""
        .as[(Int, Int)]
    }
  }

  /**
    * Loans on Dwellings For 5 or More Families (E): Total Units = 5+ Units
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionETable1 (lei: String, msaMd: Int, tractToMsaMd: String): Unit = {
    db.run {
      sql"""select count(*), sum(loan_amount) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and action_taken_type = '1'
            and (total_units <> '1' and total_units <> '2' and total_units <> '3' and total_units <> '4')
            and msa_md = ${msaMd}
            and tract_to_msamd = ${tractToMsaMd}
            group by lei"""
        .as[(Int, Int)]
    }
  }

  /**
    * Nonoccupant Loans from Columns A, B, C ,& D (F): All loans from columns A through @ where Occupancy Type = 2, 3
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionFTable1 (lei: String, msaMd: Int, tractToMsaMd: String): Unit = {
    db.run {
      sql"""select count(*), sum(loan_amount) from modifiedlar2018
           |where lei = ${lei.toUpperCase}
           |and action_taken_type = '1'
           |and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
           |and (loan_purpose = 1 or loan_purpose = 2 or loan_purpose = 31 or loan_purpose = 32)
           |and (loan_type = 1 or loan_type = 2 or loan_type = 3 or loan_type = 4)
           |and (occupancy_type = 2 or occupancy_type = 3)
           |and msa_md = ${msaMd}
         and tract_to_msamd = ${tractToMsaMd}
           |group by lei"""
        .as[(Int, Int)]
    }
  }

  /**
    * Loans On Manufactured Home Dwellings From Columns A, B, C & D (G): Construction Method = 2
    * @param lei
    * @param msaMd
    * @param tractToMsaMd
    */
  def dispositionGTable1 (lei: String, msaMd: Int, tractToMsaMd: String): Unit = {
    db.run {
      sql"""select count(*), sum(loan_amount) from modifiedlar2018
            where lei = ${lei.toUpperCase}
            and action_taken_type = '1'
            and (total_units = '1' or total_units = '2' or total_units = '3' or total_units = '4')
            and (loan_purpose = 1 or loan_purpose = 2 or loan_purpose = 31 or loan_purpose = 32)
            and (loan_type = 1 or loan_type = 2 or loan_type = 3 or loan_type = 4)
            and (construction_method = '2')
            and msa_md = ${msaMd}
            and tract_to_msamd = ${tractToMsaMd}
            group by lei"""
        .as[(Int, Int)]
    }
  }

  /**
    * Deletes entries in the Modified LAR table by their LEI
    * @param lei
    * @return the number of rows removed
    */
  def msaMds(lei: String, filingYear: Int): Future[Vector[(String, String)]] =
    db.run {
      sql"""SELECT DISTINCT msa_md, msa_md_name
                         FROM modifiedlar2018 WHERE UPPER(lei) = ${lei.toUpperCase} AND filing_year = ${filingYear}"""
        .as[(String, String)]
    }

  /**
    * Deletes entries in the Modified LAR table by their LEI
    * @param lei
    * @return the number of rows removed
    */
  def deleteByLei(lei: String, filingYear: Int): Future[Int] =
    db.run(
      sqlu"DELETE FROM #${tableName} WHERE UPPER(lei) = ${lei.toUpperCase} and filing_year = $filingYear")

  /**
    * Inserts Modified Loan Application Register data that has been enhanced with Census information via the tract map
    * @param input
    * @param submissionId
    * @return
    */
  def insert(input: EnrichedModifiedLoanApplicationRegister,
             submissionId: String,
             filingYear: Int): Future[Int] =
    db.run(sqlu"""INSERT INTO #${tableName}(
            id,
            lei,
            loan_type,
            loan_purpose,
            preapproval,
            construction_method,
            occupancy_type,
            loan_amount,
            action_taken_type,
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
            filing_year
          )
          VALUES (
            ${input.mlar.id},
            ${input.mlar.lei.toUpperCase},
            ${input.mlar.loanType},
            ${input.mlar.loanPurpose},
            ${input.mlar.preapproval},
            ${input.mlar.constructionMethod},
            ${input.mlar.occupancy},
            ${input.mlar.loanAmount},
            ${input.mlar.actionTakenType},
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
            ${submissionId},
            ${filingYear}
          )
          """)

  private def safeConvertToInt(s: String): Option[Int] =
    Try(s.toInt).toOption
}
