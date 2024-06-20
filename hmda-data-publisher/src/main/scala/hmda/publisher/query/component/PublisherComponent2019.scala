package hmda.publisher.query.component


import java.sql.Timestamp
import hmda.publisher.helper.PGTableNameLoader
import hmda.publisher.query.lar.{LarEntityImpl2019, _}
import hmda.publisher.query.panel.{InstitutionEntity}
import hmda.publisher.validation.{LarData, PanelData, TsData}
import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import hmda.query.ts.TransmittalSheetEntity
import slick.basic.{DatabaseConfig, DatabasePublisher}
import slick.jdbc.{JdbcProfile, ResultSetConcurrency, ResultSetType}

import scala.concurrent.{Future}
// $COVERAGE-OFF$
trait PublisherComponent2019 extends PGTableNameLoader {

  import dbConfig.profile.api._

  abstract class InstitutionsTableBase[T](tag: Tag, tableName: String) extends Table[T](tag, tableName) {
    def lei             = column[String]("lei", O.PrimaryKey)
    def activityYear    = column[Int]("activity_year")
    def agency          = column[Int]("agency")
    def institutionType = column[Int]("institution_type")
    def id2017          = column[String]("id2017")
    def taxId           = column[String]("tax_id")
    def rssd            = column[Int]("rssd")
    def respondentName  = column[String]("respondent_name")
    def respondentState = column[String]("respondent_state")
    def respondentCity  = column[String]("respondent_city")
    def parentIdRssd    = column[Int]("parent_id_rssd")
    def parentName      = column[String]("parent_name")
    def assets          = column[Long]("assets")
    def otherLenderCode = column[Int]("other_lender_code")
    def topHolderIdRssd = column[Int]("topholder_id_rssd")
    def topHolderName   = column[String]("topholder_name")
    def hmdaFiler       = column[Boolean]("hmda_filer")
  }
  class InstitutionsTable(tag: Tag) extends InstitutionsTableBase[InstitutionEntity](tag, panel2019TableName) {
    override def * =
      (
        lei,
        activityYear,
        agency,
        institutionType,
        id2017,
        taxId,
        rssd,
        respondentName,
        respondentState,
        respondentCity,
        parentIdRssd,
        parentName,
        assets,
        otherLenderCode,
        topHolderIdRssd,
        topHolderName,
        hmdaFiler
      ) <> (InstitutionEntity.tupled, InstitutionEntity.unapply)
  }
  val institutionsTable2019 = TableQuery[InstitutionsTable]



  class InstitutionRepository2019(val config: DatabaseConfig[JdbcProfile]) extends TableRepository[InstitutionsTable, String] {

    override val table: config.profile.api.TableQuery[InstitutionsTable] =
      institutionsTable2019

    override def getId(row: InstitutionsTable): config.profile.api.Rep[Id] =
      row.lei

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)

    def insert(institution: InstitutionEntity): Future[Int] =
      db.run(table += institution)

    def findByLei(lei: String): Future[Seq[InstitutionEntity]] =
      db.run(table.filter(_.lei === lei).result)

    //(x => (x.isX && x.name == "xyz"))
    def findActiveFilers(bankIgnoreList: Array[String]): Future[Seq[InstitutionEntity]] =
      db.run(
        table
          .filter(_.hmdaFiler === true)
          .filterNot(_.lei.toUpperCase inSet bankIgnoreList)
          .result
      )

    def getAllInstitutions(): Future[Seq[InstitutionEntity]] =
      db.run(table.result)

    def deleteByLei(lei: String): Future[Int] =
      db.run(table.filter(_.lei === lei).delete)

    def count(): Future[Int] =
      db.run(table.size.result)
  }

  val transmittalSheetTable2019 = TableQuery(tag => new TransmittalSheetTable(tag, ts2019TableName))

  abstract class LarTableBase[T](tag: Tag, tableName: String) extends Table[T](tag, tableName) {

    def id                         = column[Int]("id")
    def lei                        = column[String]("lei")
    def uli                        = column[String]("uli")
    def applicationDate            = column[String]("application_date")
    def loanType                   = column[Int]("loan_type")
    def loanPurpose                = column[Int]("loan_purpose")
    def preapproval                = column[Int]("preapproval")
    def constructionMethod         = column[Int]("construction_method")
    def occupancyType              = column[Int]("occupancy_type")
    def loanAmount                 = column[Double]("loan_amount")
    def actionTakenType            = column[Int]("action_taken_type")
    def actionTakenDate            = column[Int]("action_taken_date")
    def street                     = column[String]("street")
    def city                       = column[String]("city")
    def state                      = column[String]("state")
    def zip                        = column[String]("zip")
    def county                     = column[String]("county")
    def tract                      = column[String]("tract")
    def ethnicityApplicant1        = column[String]("ethnicity_applicant_1")
    def ethnicityApplicant2        = column[String]("ethnicity_applicant_2")
    def ethnicityApplicant3        = column[String]("ethnicity_applicant_3")
    def ethnicityApplicant4        = column[String]("ethnicity_applicant_4")
    def ethnicityApplicant5        = column[String]("ethnicity_applicant_5")
    def otherHispanicApplicant     = column[String]("other_hispanic_applicant")
    def ethnicityCoApplicant1      = column[String]("ethnicity_co_applicant_1")
    def ethnicityCoApplicant2      = column[String]("ethnicity_co_applicant_2")
    def ethnicityCoApplicant3      = column[String]("ethnicity_co_applicant_3")
    def ethnicityCoApplicant4      = column[String]("ethnicity_co_applicant_4")
    def ethnicityCoApplicant5      = column[String]("ethnicity_co_applicant_5")
    def otherHispanicCoApplicant   = column[String]("other_hispanic_co_applicant")
    def ethnicityObservedApplicant = column[Int]("ethnicity_observed_applicant")
    def ethnicityObservedCoApplicant =
      column[Int]("ethnicity_observed_co_applicant")
    def raceApplicant1           = column[String]("race_applicant_1")
    def raceApplicant2           = column[String]("race_applicant_2")
    def raceApplicant3           = column[String]("race_applicant_3")
    def raceApplicant4           = column[String]("race_applicant_4")
    def raceApplicant5           = column[String]("race_applicant_5")
    def otherNativeRaceApplicant = column[String]("other_native_race_applicant")
    def otherAsianRaceApplicant  = column[String]("other_asian_race_applicant")
    def otherPacificRaceApplicant =
      column[String]("other_pacific_race_applicant")
    def raceCoApplicant1 = column[String]("race_co_applicant_1")
    def raceCoApplicant2 = column[String]("race_co_applicant_2")
    def raceCoApplicant3 = column[String]("race_co_applicant_3")
    def raceCoApplicant4 = column[String]("race_co_applicant_4")
    def raceCoApplicant5 = column[String]("race_co_applicant_5")
    def otherNativeRaceCoApplicant =
      column[String]("other_native_race_co_applicant")
    def otherAsianRaceCoApplicant =
      column[String]("other_asian_race_co_applicant")
    def otherPacificRaceCoApplicant =
      column[String]("other_pacific_race_co_applicant")
    def raceObservedApplicant    = column[Int]("race_observed_applicant")
    def raceObservedCoApplicant  = column[Int]("race_observed_co_applicant")
    def sexApplicant             = column[Int]("sex_applicant")
    def sexCoApplicant           = column[Int]("sex_co_applicant")
    def observedSexApplicant     = column[Int]("observed_sex_applicant")
    def observedSexCoApplicant   = column[Int]("observed_sex_co_applicant")
    def ageApplicant             = column[Int]("age_applicant")
    def ageCoApplicant           = column[Int]("age_co_applicant")
    def income                   = column[String]("income")
    def purchaserType            = column[Int]("purchaser_type")
    def rateSpread               = column[String]("rate_spread")
    def hoepaStatus              = column[Int]("hoepa_status")
    def lienStatus               = column[Int]("lien_status")
    def creditScoreApplicant     = column[Int]("credit_score_applicant")
    def creditScoreCoApplicant   = column[Int]("credit_score_co_applicant")
    def creditScoreTypeApplicant = column[Int]("credit_score_type_applicant")
    def creditScoreModelApplicant =
      column[String]("credit_score_model_applicant")
    def creditScoreTypeCoApplicant =
      column[Int]("credit_score_type_co_applicant")
    def creditScoreModelCoApplicant =
      column[String]("credit_score_model_co_applicant")
    def denialReason1           = column[String]("denial_reason1")
    def denialReason2           = column[String]("denial_reason2")
    def denialReason3           = column[String]("denial_reason3")
    def denialReason4           = column[String]("denial_reason4")
    def otherDenialReason       = column[String]("other_denial_reason")
    def totalLoanCosts          = column[String]("total_loan_costs")
    def totalPoints             = column[String]("total_points")
    def originationCharges      = column[String]("origination_charges")
    def discountPoints          = column[String]("discount_points")
    def lenderCredits           = column[String]("lender_credits")
    def interestRate            = column[String]("interest_rate")
    def paymentPenalty          = column[String]("payment_penalty")
    def debtToIncome            = column[String]("debt_to_incode")
    def loanValueRatio          = column[String]("loan_value_ratio")
    def loanTerm                = column[String]("loan_term")
    def rateSpreadIntro         = column[String]("rate_spread_intro")
    def baloonPayment           = column[Int]("baloon_payment")
    def insertOnlyPayment       = column[Int]("insert_only_payment")
    def amortization            = column[Int]("amortization")
    def otherAmortization       = column[Int]("other_amortization")
    def propertyValue           = column[String]("property_value")
    def homeSecurityPolicy      = column[Int]("home_security_policy")
    def landPropertyInterest    = column[Int]("lan_property_interest")
    def totalUnits              = column[Int]("total_uits")
    def mfAffordable            = column[String]("mf_affordable")
    def applicationSubmission   = column[Int]("application_submission")
    def payable                 = column[Int]("payable")
    def nmls                    = column[String]("nmls")
    def aus1                    = column[String]("aus1")
    def aus2                    = column[String]("aus2")
    def aus3                    = column[String]("aus3")
    def aus4                    = column[String]("aus4")
    def aus5                    = column[String]("aus5")
    def otheraus                = column[String]("other_aus")
    def aus1Result              = column[String]("aus1_result")
    def aus2Result              = column[String]("aus2_result")
    def aus3Result              = column[String]("aus3_result")
    def aus4Result              = column[String]("aus4_result")
    def aus5Result              = column[String]("aus5_result")
    def otherAusResult          = column[String]("other_aus_result")
    def reverseMortgage         = column[Int]("reverse_mortgage")
    def lineOfCredits           = column[Int]("line_of_credits")
    def businessOrCommercial    = column[Int]("business_or_commercial")
    def conformingLoanLimit     = column[String]("conforming_loan_limit")
    def ethnicityCategorization = column[String]("ethnicity_categorization")
    def raceCategorization      = column[String]("race_categorization")
    def sexCategorization       = column[String]("sex_categorization")
    def dwellingCategorization  = column[String]("dwelling_categorization")
    def loanProductTypeCategorization =
      column[String]("loan_product_type_categorization")
    def tractPopulation = column[Int]("tract_population")
    def tractMinorityPopulationPercent =
      column[Double]("tract_minority_population_percent")
    def tractMedianIncome  = column[Int]("ffiec_msa_md_median_family_income")
    def tractOccupiedUnits = column[Int]("tract_owner_occupied_units")
    def tractOneToFourFamilyUnits =
      column[Int]("tract_one_to_four_family_homes")
    def tractMedianAge = column[Int]("tract_median_age_of_housing_units")
    def tractToMsaIncomePercent =
      column[Double]("tract_to_msa_income_percentage")

    def larEntityImpl2019Projection =
      (
        larPartOneProjection,
        larPartTwoProjection,
        larPartThreeProjection,
        larPartFourProjection,
        larPartFiveProjection,
        larPartSixProjection,
        larPartSevenProjection
      ) <> ((LarEntityImpl2019.apply _).tupled, LarEntityImpl2019.unapply)

    def larPartOneProjection =
      (
        id,
        lei,
        uli,
        applicationDate,
        loanType,
        loanPurpose,
        preapproval,
        constructionMethod,
        occupancyType,
        loanAmount,
        actionTakenType,
        actionTakenDate,
        street,
        city,
        state,
        zip,
        county,
        tract
      ) <> ((LarPartOne2019.apply _).tupled, LarPartOne2019.unapply)

    def larPartTwoProjection =
      (
        ethnicityApplicant1,
        ethnicityApplicant2,
        ethnicityApplicant3,
        ethnicityApplicant4,
        ethnicityApplicant5,
        otherHispanicApplicant,
        ethnicityCoApplicant1,
        ethnicityCoApplicant2,
        ethnicityCoApplicant3,
        ethnicityCoApplicant4,
        ethnicityCoApplicant5,
        otherHispanicCoApplicant,
        ethnicityObservedApplicant,
        ethnicityObservedCoApplicant,
        raceApplicant1,
        raceApplicant2,
        raceApplicant3,
        raceApplicant4,
        raceApplicant5
      ) <> ((LarPartTwo2019.apply _).tupled, LarPartTwo2019.unapply)

    def larPartThreeProjection =
      (
        otherNativeRaceApplicant,
        otherAsianRaceApplicant,
        otherPacificRaceApplicant,
        raceCoApplicant1,
        raceCoApplicant2,
        raceCoApplicant3,
        raceCoApplicant4,
        raceCoApplicant5,
        otherNativeRaceCoApplicant,
        otherAsianRaceCoApplicant,
        otherPacificRaceCoApplicant,
        raceObservedApplicant,
        raceObservedCoApplicant,
        sexApplicant,
        sexCoApplicant,
        observedSexApplicant,
        observedSexCoApplicant,
        ageApplicant,
        ageCoApplicant,
        income
      ) <> ((LarPartThree2019.apply _).tupled, LarPartThree2019.unapply)

    def larPartFourProjection =
      (
        purchaserType,
        rateSpread,
        hoepaStatus,
        lienStatus,
        creditScoreApplicant,
        creditScoreCoApplicant,
        creditScoreTypeApplicant,
        creditScoreModelApplicant,
        creditScoreTypeCoApplicant,
        creditScoreModelCoApplicant,
        denialReason1,
        denialReason2,
        denialReason3,
        denialReason4,
        otherDenialReason,
        totalLoanCosts,
        totalPoints,
        originationCharges
      ) <> ((LarPartFour2019.apply _).tupled, LarPartFour2019.unapply)

    def larPartFiveProjection =
      (
        discountPoints,
        lenderCredits,
        interestRate,
        paymentPenalty,
        debtToIncome,
        loanValueRatio,
        loanTerm,
        rateSpreadIntro,
        baloonPayment,
        insertOnlyPayment,
        amortization,
        otherAmortization,
        propertyValue,
        homeSecurityPolicy,
        landPropertyInterest,
        totalUnits,
        mfAffordable,
        applicationSubmission
      ) <> ((LarPartFive2019.apply _).tupled, LarPartFive2019.unapply)

    def larPartSixProjection =
      (
        payable,
        nmls,
        aus1,
        aus2,
        aus3,
        aus4,
        aus5,
        otheraus,
        aus1Result,
        aus2Result,
        aus3Result,
        aus4Result,
        aus5Result,
        otherAusResult,
        reverseMortgage,
        lineOfCredits,
        businessOrCommercial
      ) <> ((LarPartSix2019.apply _).tupled, LarPartSix2019.unapply)

    def larPartSevenProjection =
      (
        conformingLoanLimit,
        ethnicityCategorization,
        raceCategorization,
        sexCategorization,
        dwellingCategorization,
        loanProductTypeCategorization,
        tractPopulation,
        tractMinorityPopulationPercent,
        tractMedianIncome,
        tractOccupiedUnits,
        tractOneToFourFamilyUnits,
        tractMedianAge,
        tractToMsaIncomePercent
      ) <> ((LarPartSeven2019.apply _).tupled, LarPartSeven2019.unapply)

  }

  class LarTable(tag: Tag) extends LarTableBase[LarEntityImpl2019](tag, lar2019TableName) {
    def * = larEntityImpl2019Projection
  }



  val larTable2019 = TableQuery[LarTable]


  class LarRepository2019(val config: DatabaseConfig[JdbcProfile]) extends TableRepository[LarTable, String] {

    override val table: config.profile.api.TableQuery[LarTable] =
      larTable2019

    override def getId(row: LarTable): config.profile.api.Rep[Id] =
      row.lei

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)

    def insert(ts: LarEntityImpl2019): Future[Int] =
      db.run(table += ts)

    def findByLei(lei: String): Future[Seq[LarEntityImpl2019]] =
      db.run(table.filter(_.lei === lei).result)

    def deleteByLei(lei: String): Future[Int] =
      db.run(table.filter(_.lei === lei).delete)
    def count(): Future[Int] =
      db.run(table.size.result)

    def getAllLARsCount(bankIgnoreList: Array[String]): Future[Int] =
      db.run(getAllLARsQuery(bankIgnoreList).size.result)

    def getAllLARs(bankIgnoreList: Array[String]): DatabasePublisher[LarEntityImpl2019] =
      db.stream(
        getAllLARsQuery(bankIgnoreList).result
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      )
    protected def getAllLARsQuery(bankIgnoreList: Array[String]): Query[LarTable, LarEntityImpl2019, Seq] =
      table.filterNot(_.lei.toUpperCase inSet bankIgnoreList)
  }


  abstract class ModifiedLarTableBase[T](tag: Tag, tableName: String) extends Table[T](tag, tableName) {

    def id                  = column[Int]("id")
    def lei                 = column[String]("lei")
    def loanType            = column[Option[Int]]("loan_type")
    def loanPurpose         = column[Option[Int]]("loan_purpose")
    def preapproval         = column[Option[Int]]("preapproval")
    def constructionMethod  = column[Option[String]]("construction_method")
    def occupancyType       = column[Option[Int]]("occupancy_type")
    def loanAmount          = column[Option[String]]("loan_amount")
    def actionTakenType     = column[Option[Int]]("action_taken_type")
    def state               = column[Option[String]]("state")
    def county              = column[Option[String]]("county")
    def tract               = column[Option[String]]("tract")
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
    def raceApplicant1        = column[Option[String]]("race_applicant_1")
    def raceApplicant2        = column[Option[String]]("race_applicant_2")
    def raceApplicant3        = column[Option[String]]("race_applicant_3")
    def raceApplicant4        = column[Option[String]]("race_applicant_4")
    def raceApplicant5        = column[Option[String]]("race_applicant_5")
    def raceCoApplicant1      = column[Option[String]]("race_co_applicant_1")
    def raceCoApplicant2      = column[Option[String]]("race_co_applicant_2")
    def raceCoApplicant3      = column[Option[String]]("race_co_applicant_3")
    def raceCoApplicant4      = column[Option[String]]("race_co_applicant_4")
    def raceCoApplicant5      = column[Option[String]]("race_co_applicant_5")
    def raceObservedApplicant = column[Option[Int]]("race_observed_applicant")
    def raceObservedCoApplicant =
      column[Option[Int]]("race_observed_co_applicant")
    def sexApplicant         = column[Option[Int]]("sex_applicant")
    def sexCoApplicant       = column[Option[Int]]("sex_co_applicant")
    def observedSexApplicant = column[Option[Int]]("observed_sex_applicant")
    def observedSexCoApplicant =
      column[Option[Int]]("observed_sex_co_applicant")
    def ageApplicant   = column[Option[String]]("age_applicant")
    def ageCoApplicant = column[Option[String]]("age_co_applicant")
    def income         = column[Option[String]]("income")
    def purchaserType  = column[Option[Int]]("purchaser_type")
    def rateSpread     = column[Option[String]]("rate_spread")
    def hoepaStatus    = column[Option[Int]]("hoepa_status")
    def lienStatus     = column[Option[Int]]("lien_status")
    def creditScoreTypeApplicant =
      column[Option[Int]]("credit_score_type_applicant")
    def creditScoreTypeCoApplicant =
      column[Option[Int]]("credit_score_type_co_applicant")
    def denialReason1         = column[Option[Int]]("denial_reason1")
    def denialReason2         = column[Option[Int]]("denial_reason2")
    def denialReason3         = column[Option[Int]]("denial_reason3")
    def denialReason4         = column[Option[Int]]("denial_reason4")
    def totalLoanCosts        = column[Option[String]]("total_loan_costs")
    def totalPoints           = column[Option[String]]("total_points")
    def originationCharges    = column[Option[String]]("origination_charges")
    def discountPoints        = column[Option[String]]("discount_points")
    def lenderCredits         = column[Option[String]]("lender_credits")
    def interestRate          = column[Option[String]]("interest_rate")
    def paymentPenalty        = column[Option[String]]("payment_penalty")
    def debtToIncome          = column[Option[String]]("debt_to_incode")
    def loanValueRatio        = column[Option[String]]("loan_value_ratio")
    def loanTerm              = column[Option[String]]("loan_term")
    def rateSpreadIntro       = column[Option[String]]("rate_spread_intro")
    def baloonPayment         = column[Option[Int]]("baloon_payment")
    def insertOnlyPayment     = column[Option[Int]]("insert_only_payment")
    def amortization          = column[Option[Int]]("amortization")
    def otherAmortization     = column[Option[Int]]("other_amortization")
    def propertyValue         = column[String]("property_value")
    def homeSecurityPolicy    = column[Option[Int]]("home_security_policy")
    def landPropertyInterest  = column[Option[Int]]("lan_property_interest")
    def totalUnits            = column[Option[String]]("total_units")
    def mfAffordable          = column[Option[String]]("mf_affordable")
    def applicationSubmission = column[Option[Int]]("application_submission")
    def payable               = column[Option[Int]]("payable")
    def aus1                  = column[Option[Int]]("aus1")
    def aus2                  = column[Option[Int]]("aus2")
    def aus3                  = column[Option[Int]]("aus3")
    def aus4                  = column[Option[Int]]("aus4")
    def aus5                  = column[Option[Int]]("aus5")
    def reverseMortgage       = column[Option[Int]]("reverse_mortgage")
    def lineOfCredits         = column[Option[Int]]("line_of_credits")
    def businessOrCommercial  = column[Option[Int]]("business_or_commercial")
    def filingYear            = column[Option[Int]]("filing_year")
    def msaMd                 = column[Option[Int]]("msa_md")
    def conformingLoanLimit   = column[Option[String]]("conforming_loan_limit")
    def loanFlag              = column[Option[String]]("loan_flag")
    def dwellingCategory      = column[Option[String]]("dwelling_category")
    def loanProductType       = column[Option[String]]("loan_product_type")
    def ethnicityCategorization =
      column[Option[String]]("ethnicity_categorization")
    def raceCategorization = column[Option[String]]("race_categorization")
    def sexCategorization  = column[Option[String]]("sex_categorization")
    def applicantAgeGreaterThan62 =
      column[Option[String]]("applicant_age_greater_than_62")
    def coapplicantAgeGreaterThan62 =
      column[Option[String]]("coapplicant_age_greater_than_62")
    def population = column[Option[String]]("population")
    def minorityPopulationPercent =
      column[Option[String]]("minority_population_percent")
    def ffiecMedFamIncome      = column[Option[String]]("ffiec_med_fam_income")
    def medianIncomePercentage = column[Option[Double]]("median_income_percentage")
    def ownerOccupiedUnits     = column[Option[String]]("owner_occupied_units")
    def oneToFourFamUnits      = column[Option[String]]("one_to_four_fam_units")
    def medianAge              = column[Option[Int]]("median_age")
    def tractToMsamd           = column[Option[String]]("tract_to_msamd")
    def medianAgeCalculated    = column[Option[String]]("median_age_calculated")
    def percentMedianMsaIncome =
      column[Option[Double]]("percent_median_msa_income")
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
  }

  class ModifiedLarTable(tag: Tag) extends ModifiedLarTableBase[ModifiedLarEntityImpl](tag, mlar2019TableName) {
    override def * = modifiedLarEntityImplProjection
  }

  val mlarTable2019 = TableQuery[ModifiedLarTable]

  class ModifiedLarRepository2019(val config: DatabaseConfig[JdbcProfile]) extends TableRepository[ModifiedLarTable, String] {

    override val table: config.profile.api.TableQuery[ModifiedLarTable] =
      mlarTable2019

    override def getId(row: ModifiedLarTable): config.profile.api.Rep[Id] =
      row.lei

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)

    def insert(ts: ModifiedLarEntityImpl): Future[Int] =
      db.run(table += ts)

    def findByLei(lei: String): Future[Seq[ModifiedLarEntityImpl]] =
      db.run(table.filter(_.lei === lei).result)

    def deleteByLei(lei: String): Future[Int] =
      db.run(table.filter(_.lei === lei).delete)

    def count(): Future[Int] =
      db.run(table.size.result)

    def getAllLARs(bankIgnoreList: Array[String]): DatabasePublisher[ModifiedLarEntityImpl] =
      db.stream(
        table
          .filterNot(_.lei.toUpperCase inSet bankIgnoreList)
          .result
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
          .transactionally
      )
  }


  val validationLarData2019: LarData = LarData[LarEntityImpl2019, LarTable](larTable2019)(_.lei)
  val validationMLarData2019: LarData =
    LarData[ModifiedLarEntityImpl, ModifiedLarTable](mlarTable2019)(_.lei)
  val validationTSData2019: TsData =
    TsData[TransmittalSheetEntity, TransmittalSheetTable](transmittalSheetTable2019)(_.lei, _.totalLines, _.submissionId)
  val validationPanelData2019: PanelData =
    PanelData[InstitutionEntity, InstitutionsTable](institutionsTable2019)(_.lei,_.hmdaFiler)
}
// $COVERAGE-ON$