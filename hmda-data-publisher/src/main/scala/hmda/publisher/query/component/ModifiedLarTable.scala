package hmda.publisher.query.component

import hmda.publisher.query.lar._
import hmda.query.DbConfiguration.dbConfig.profile.api._

import java.sql.Timestamp

class ModifiedLarTable(tag: Tag, tableName: String) extends Table[ModifiedLarEntityImpl](tag, tableName){

  def id = column[Int]("id")
  def lei = column[String]("lei")
  def loanType = column[Option[Int]]("loan_type")
  def loanPurpose = column[Option[Int]]("loan_purpose")
  def preapproval = column[Option[Int]]("preapproval")
  def constructionMethod = column[Option[String]]("construction_method")
  def occupancyType = column[Option[Int]]("occupancy_type")
  def loanAmount = column[Option[String]]("loan_amount")
  def actionTakenType = column[Option[Int]]("action_taken_type")
  def state = column[Option[String]]("state")
  def county = column[Option[String]]("county")
  def tract = column[Option[String]]("tract")
  def ethnicityApplicant1 = column[Option[String]]("ethnicity_applicant_1")
  def ethnicityApplicant2 = column[Option[String]]("ethnicity_applicant_2")
  def ethnicityApplicant3 = column[Option[String]]("ethnicity_applicant_3")
  def ethnicityApplicant4 = column[Option[String]]("ethnicity_applicant_4")
  def ethnicityApplicant5 = column[Option[String]]("ethnicity_applicant_5")
  def ethnicityCoApplicant1 =
    column[Option[String]]("ethnicity_co_applicant_1")
  def ethnicityCoApplicant2 =
    column[Option[String]]("ethnicity_co_applicant_2")
  def ethnicityCoApplicant3 =
    column[Option[String]]("ethnicity_co_applicant_3")
  def ethnicityCoApplicant4 =
    column[Option[String]]("ethnicity_co_applicant_4")
  def ethnicityCoApplicant5 =
    column[Option[String]]("ethnicity_co_applicant_5")
  def ethnicityObservedApplicant =
    column[Option[Int]]("ethnicity_observed_applicant")
  def ethnicityObservedCoApplicant =
    column[Option[Int]]("ethnicity_observed_co_applicant")
  def raceApplicant1 = column[Option[String]]("race_applicant_1")
  def raceApplicant2 = column[Option[String]]("race_applicant_2")
  def raceApplicant3 = column[Option[String]]("race_applicant_3")
  def raceApplicant4 = column[Option[String]]("race_applicant_4")
  def raceApplicant5 = column[Option[String]]("race_applicant_5")
  def raceCoApplicant1 = column[Option[String]]("race_co_applicant_1")
  def raceCoApplicant2 = column[Option[String]]("race_co_applicant_2")
  def raceCoApplicant3 = column[Option[String]]("race_co_applicant_3")
  def raceCoApplicant4 = column[Option[String]]("race_co_applicant_4")
  def raceCoApplicant5 = column[Option[String]]("race_co_applicant_5")
  def raceObservedApplicant = column[Option[Int]]("race_observed_applicant")
  def raceObservedCoApplicant =
    column[Option[Int]]("race_observed_co_applicant")
  def sexApplicant = column[Option[Int]]("sex_applicant")
  def sexCoApplicant = column[Option[Int]]("sex_co_applicant")
  def observedSexApplicant = column[Option[Int]]("observed_sex_applicant")
  def observedSexCoApplicant =
    column[Option[Int]]("observed_sex_co_applicant")
  def ageApplicant = column[Option[String]]("age_applicant")
  def ageCoApplicant = column[Option[String]]("age_co_applicant")
  def income = column[Option[String]]("income")
  def purchaserType = column[Option[Int]]("purchaser_type")
  def rateSpread = column[Option[String]]("rate_spread")
  def hoepaStatus = column[Option[Int]]("hoepa_status")
  def lienStatus = column[Option[Int]]("lien_status")
  def creditScoreTypeApplicant =
    column[Option[Int]]("credit_score_type_applicant")
  def creditScoreTypeCoApplicant =
    column[Option[Int]]("credit_score_type_co_applicant")
  def denialReason1 = column[Option[Int]]("denial_reason1")
  def denialReason2 = column[Option[Int]]("denial_reason2")
  def denialReason3 = column[Option[Int]]("denial_reason3")
  def denialReason4 = column[Option[Int]]("denial_reason4")
  def totalLoanCosts = column[Option[String]]("total_loan_costs")
  def totalPoints = column[Option[String]]("total_points")
  def originationCharges = column[Option[String]]("origination_charges")
  def discountPoints = column[Option[String]]("discount_points")
  def lenderCredits = column[Option[String]]("lender_credits")
  def interestRate = column[Option[String]]("interest_rate")
  def paymentPenalty = column[Option[String]]("payment_penalty")
  def debtToIncome = column[Option[String]]("debt_to_incode")
  def loanValueRatio = column[Option[String]]("loan_value_ratio")
  def loanTerm = column[Option[String]]("loan_term")
  def rateSpreadIntro = column[Option[String]]("rate_spread_intro")
  def baloonPayment = column[Option[Int]]("baloon_payment")
  def insertOnlyPayment = column[Option[Int]]("insert_only_payment")
  def amortization = column[Option[Int]]("amortization")
  def otherAmortization = column[Option[Int]]("other_amortization")
  def propertyValue = column[String]("property_value")
  def homeSecurityPolicy = column[Option[Int]]("home_security_policy")
  def landPropertyInterest = column[Option[Int]]("lan_property_interest")
  def totalUnits = column[Option[String]]("total_units")
  def mfAffordable = column[Option[String]]("mf_affordable")
  def applicationSubmission = column[Option[Int]]("application_submission")
  def payable = column[Option[Int]]("payable")
  def aus1 = column[Option[Int]]("aus1")
  def aus2 = column[Option[Int]]("aus2")
  def aus3 = column[Option[Int]]("aus3")
  def aus4 = column[Option[Int]]("aus4")
  def aus5 = column[Option[Int]]("aus5")
  def reverseMortgage = column[Option[Int]]("reverse_mortgage")
  def lineOfCredits = column[Option[Int]]("line_of_credits")
  def businessOrCommercial = column[Option[Int]]("business_or_commercial")
  def filingYear = column[Option[Int]]("filing_year")
  def msaMd = column[Option[Int]]("msa_md")
  def conformingLoanLimit = column[Option[String]]("conforming_loan_limit")
  def loanFlag = column[Option[String]]("loan_flag")
  def dwellingCategory = column[Option[String]]("dwelling_category")
  def loanProductType = column[Option[String]]("loan_product_type")
  def ethnicityCategorization =
    column[Option[String]]("ethnicity_categorization")
  def raceCategorization = column[Option[String]]("race_categorization")
  def sexCategorization = column[Option[String]]("sex_categorization")
  def applicantAgeGreaterThan62 =
    column[Option[String]]("applicant_age_greater_than_62")
  def coapplicantAgeGreaterThan62 =
    column[Option[String]]("coapplicant_age_greater_than_62")
  def population = column[Option[String]]("population")
  def minorityPopulationPercent =
    column[Option[String]]("minority_population_percent")
  def ffiecMedFamIncome = column[Option[String]]("ffiec_med_fam_income")
  def medianIncomePercentage = column[Option[Double]]("median_income_percentage")
  def ownerOccupiedUnits = column[Option[String]]("owner_occupied_units")
  def oneToFourFamUnits = column[Option[String]]("one_to_four_fam_units")
  def medianAge = column[Option[Int]]("median_age")
  def tractToMsamd = column[Option[String]]("tract_to_msamd")
  def medianAgeCalculated = column[Option[String]]("median_age_calculated")
  def percentMedianMsaIncome =
    column[Option[String]]("percent_median_msa_income")
  def msaMDName = column[Option[String]]("msa_md_name")
  def createdAt = column[Timestamp]("created_at")
  def modifiedLarEntityImplProjection =
    (
      mlarPartOneProjection,
      mlarPartTwoProjection,
      mlarPartThreeProjection,
      mlarPartFourProjection,
      mlarPartFiveProjection,
      mlarPartSixProjection
    ) <> ((ModifiedLarEntityImpl.apply _).tupled, ModifiedLarEntityImpl.unapply)
  def mlarPartOneProjection =
    (
      filingYear,
      lei,
      msaMd,
      state,
      county,
      tract,
      conformingLoanLimit,
      loanFlag,
      loanProductType,
      dwellingCategory,
      ethnicityCategorization,
      raceCategorization,
      sexCategorization,
      actionTakenType,
      purchaserType,
      preapproval,
      loanType,
      loanPurpose,
      lienStatus
    ) <> ((ModifiedLarPartOne.apply _).tupled, ModifiedLarPartOne.unapply)
  def mlarPartTwoProjection =
    (
      reverseMortgage,
      lineOfCredits,
      businessOrCommercial,
      loanAmount,
      loanValueRatio,
      interestRate,
      rateSpread,
      hoepaStatus,
      totalLoanCosts,
      totalPoints,
      originationCharges,
      discountPoints,
      lenderCredits,
      loanTerm,
      paymentPenalty,
      rateSpreadIntro,
      amortization,
      insertOnlyPayment
    ) <> ((ModifiedLarPartTwo.apply _).tupled, ModifiedLarPartTwo.unapply)
  def mlarPartThreeProjection =
    (
      baloonPayment,
      otherAmortization,
      propertyValue,
      constructionMethod,
      occupancyType,
      homeSecurityPolicy,
      landPropertyInterest,
      totalUnits,
      mfAffordable,
      income,
      debtToIncome,
      creditScoreTypeApplicant,
      creditScoreTypeCoApplicant,
      ethnicityApplicant1,
      ethnicityApplicant2,
      ethnicityApplicant3,
      ethnicityApplicant4
    ) <> ((ModifiedLarPartThree.apply _).tupled, ModifiedLarPartThree.unapply)
  def mlarPartFourProjection =
    (
      ethnicityApplicant5,
      ethnicityCoApplicant1,
      ethnicityCoApplicant2,
      ethnicityCoApplicant3,
      ethnicityCoApplicant4,
      ethnicityCoApplicant5,
      ethnicityObservedApplicant,
      ethnicityObservedCoApplicant,
      raceApplicant1,
      raceApplicant2,
      raceApplicant3,
      raceApplicant4,
      raceApplicant5,
      raceCoApplicant1,
      raceCoApplicant2,
      raceCoApplicant3,
      raceCoApplicant4
    ) <> ((ModifiedLarPartFour.apply _).tupled, ModifiedLarPartFour.unapply)
  def mlarPartFiveProjection =
    (
      raceCoApplicant5,
      raceObservedApplicant,
      raceObservedCoApplicant,
      sexApplicant,
      sexCoApplicant,
      observedSexApplicant,
      observedSexCoApplicant,
      ageApplicant,
      ageCoApplicant,
      applicantAgeGreaterThan62,
      coapplicantAgeGreaterThan62,
      applicationSubmission,
      payable,
      aus1,
      aus2,
      aus3,
      aus4
    ) <> ((ModifiedLarPartFive.apply _).tupled, ModifiedLarPartFive.unapply)
  def mlarPartSixProjection =
    (
      aus5,
      denialReason1,
      denialReason2,
      denialReason3,
      denialReason4,
      population,
      minorityPopulationPercent,
      ffiecMedFamIncome,
      medianIncomePercentage,
      ownerOccupiedUnits,
      oneToFourFamUnits,
      medianAge
    ) <> ((ModifiedLarPartSix.apply _).tupled, ModifiedLarPartSix.unapply)

  override def * = modifiedLarEntityImplProjection
}
