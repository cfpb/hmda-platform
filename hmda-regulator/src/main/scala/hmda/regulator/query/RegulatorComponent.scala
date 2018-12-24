package hmda.regulator.query

import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import hmda.regulator.query.lar._
import hmda.regulator.query.panel.{InstitutionEmailEntity, InstitutionEntity}
import hmda.regulator.query.ts.TransmittalSheetEntity
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait RegulatorComponent {

  import dbConfig.profile.api._

  class InstitutionsTable(tag: Tag)
      extends Table[InstitutionEntity](tag, "institutions2018") {
    def lei = column[String]("lei", O.PrimaryKey)
    def activityYear = column[Int]("activity_year")
    def agency = column[Int]("agency")
    def institutionType = column[Int]("institution_type")
    def id2017 = column[String]("id2017")
    def taxId = column[String]("tax_id")
    def rssd = column[Int]("rssd")
    def respondentName = column[String]("respondent_name")
    def respondentState = column[String]("respondent_state")
    def respondentCity = column[String]("respondent_city")
    def parentIdRssd = column[Int]("parent_id_rssd")
    def parentName = column[String]("parent_name")
    def assets = column[Int]("assets")
    def otherLenderCode = column[Int]("other_lender_code")
    def topHolderIdRssd = column[Int]("topholder_id_rssd")
    def topHolderName = column[String]("topholder_name")
    def hmdaFiler = column[Boolean]("hmda_filer")

    override def * =
      (lei,
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
       hmdaFiler) <> (InstitutionEntity.tupled, InstitutionEntity.unapply)
  }
  val institutionsTable = TableQuery[InstitutionsTable]

  class InstitutionRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[InstitutionsTable, String] {

    override val table: config.profile.api.TableQuery[InstitutionsTable] =
      institutionsTable

    override def getId(row: InstitutionsTable): config.profile.api.Rep[Id] =
      row.lei

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)

    def insert(institution: InstitutionEntity): Future[Int] = {
      db.run(table += institution)
    }

    def findByLei(lei: String): Future[Seq[InstitutionEntity]] = {
      db.run(table.filter(_.lei === lei).result)
    }

    def findActiveFilers(): Future[Seq[InstitutionEntity]] = {
      db.run(table.filter(_.hmdaFiler === true).result)
    }

    def getAllInstitutions(): Future[Seq[InstitutionEntity]] = {
      db.run(table.result)
    }

    def deleteByLei(lei: String): Future[Int] = {
      db.run(table.filter(_.lei === lei).delete)
    }

    def count(): Future[Int] = {
      db.run(table.size.result)
    }
  }

  class InstitutionEmailsTable(tag: Tag)
      extends Table[InstitutionEmailEntity](tag, "institutions_emails_2018") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lei = column[String]("lei")
    def emailDomain = column[String]("email_domain")

    def * =
      (id, lei, emailDomain) <> (InstitutionEmailEntity.tupled, InstitutionEmailEntity.unapply)

    def institutionFK =
      foreignKey("INST_FK", lei, institutionsTable)(
        _.lei,
        onUpdate = ForeignKeyAction.Restrict,
        onDelete = ForeignKeyAction.Cascade)
  }

  val institutionEmailsTable = TableQuery[InstitutionEmailsTable]

  class InstitutionEmailsRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[InstitutionEmailsTable, Int] {
    val table = institutionEmailsTable
    def getId(table: InstitutionEmailsTable) = table.id
    def deleteById(id: Int) = db.run(filterById(id).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)

    def findByLei(lei: String) = {
      db.run(table.filter(_.lei === lei).result)
    }
  }
  class TransmittalSheetTable(tag: Tag)
      extends Table[TransmittalSheetEntity](tag, "transmittalsheet2018") {

    def lei = column[String]("lei", O.PrimaryKey)
    def id = column[Int]("id")
    def institutionName = column[String]("institution_name")
    def year = column[Int]("year")
    def quarter = column[Int]("quarter")
    def name = column[String]("name")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def street = column[String]("street")
    def city = column[String]("city")
    def state = column[String]("state")
    def zipCode = column[String]("zip_code")
    def agency = column[Int]("agency")
    def totalLines = column[Int]("total_lines")
    def taxId = column[String]("tax_id")

    override def * =
      (
        lei,
        id,
        institutionName,
        year,
        quarter,
        name,
        phone,
        email,
        street,
        city,
        state,
        zipCode,
        agency,
        totalLines,
        taxId
      ) <> (TransmittalSheetEntity.tupled, TransmittalSheetEntity.unapply)
  }

  val transmittalSheetTable = TableQuery[TransmittalSheetTable]

  class TransmittalSheetRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[TransmittalSheetTable, String] {

    override val table: config.profile.api.TableQuery[TransmittalSheetTable] =
      transmittalSheetTable

    override def getId(row: TransmittalSheetTable): config.profile.api.Rep[Id] =
      row.lei

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)

    def insert(ts: TransmittalSheetEntity): Future[Int] = {
      db.run(table += ts)
    }

    def findByLei(lei: String): Future[Seq[TransmittalSheetEntity]] = {
      db.run(table.filter(_.lei === lei).result)
    }

    def deleteByLei(lei: String): Future[Int] = {
      db.run(table.filter(_.lei === lei).delete)
    }

    def count(): Future[Int] = {
      db.run(table.size.result)
    }

    def getAllSheets(): Future[Seq[TransmittalSheetEntity]] = {
      db.run(table.result)
    }
  }

  class LarTable(tag: Tag)
      extends Table[LarEntityImpl](tag, "loanapplicationregister2018") {

    def id = column[Int]("id")
    def lei = column[String]("lei")
    def uli = column[String]("uli")
    def appDate = column[String]("appDate")
    def loanType = column[Int]("loanType")
    def loanPurpose = column[Int]("loanPurpose")
    def preapproval = column[Int]("preapproval")
    def constructionMethod = column[Int]("constructionMethod")
    def occupancyType = column[Int]("occupancyType")
    def loanAmount = column[Double]("loanAmount")
    def actionTakenType = column[Int]("actionTakenType")
    def actionTakenDate = column[Int]("actionTakenDate")
    def street = column[String]("street")
    def city = column[String]("city")
    def state = column[String]("state")
    def zip = column[String]("zip")
    def county = column[String]("county")
    def tract = column[String]("tract")
    def ethnicityApplicant1 = column[Int]("ethnicityApplicant1")
    def ethnicityApplicant2 = column[Int]("ethnicityApplicant2")
    def ethnicityApplicant3 = column[Int]("ethnicityApplicant3")
    def ethnicityApplicant4 = column[Int]("ethnicityApplicant4")
    def ethnicityApplicant5 = column[Int]("ethnicityApplicant5")
    def otherHispanicApplicant = column[String]("otherHispanicApplicant")
    def ethnicityCoApplicant1 = column[Int]("ethnicityCoApplicant1")
    def ethnicityCoApplicant2 = column[Int]("ethnicityCoApplicant2")
    def ethnicityCoApplicant3 = column[Int]("ethnicityCoApplicant3")
    def ethnicityCoApplicant4 = column[Int]("ethnicityCoApplicant4")
    def ethnicityCoApplicant5 = column[Int]("ethnicityCoApplicant5")
    def otherHispanicCoApplicant = column[String]("otherHispanicCoApplicant")
    def ethnicityObservedApplicant = column[Int]("ethnicityObservedApplicant")
    def ethnicityObservedCoApplicant =
      column[Int]("ethnicityObservedCoApplicant")
    def raceApplicant1 = column[Int]("raceApplicant1")
    def raceApplicant2 = column[Int]("raceApplicant2")
    def raceApplicant3 = column[Int]("raceApplicant3")
    def raceApplicant4 = column[Int]("raceApplicant4")
    def raceApplicant5 = column[Int]("raceApplicant5")
    def otherNativeRaceApplicant = column[String]("otherNativeRaceApplicant")
    def otherAsianRaceApplicant = column[String]("otherAsianRaceApplicant")
    def otherPacificRaceApplicant = column[String]("otherPacificRaceApplicant")
    def rateCoApplicant1 = column[Int]("rateCoApplicant1")
    def rateCoApplicant2 = column[Int]("rateCoApplicant2")
    def rateCoApplicant3 = column[Int]("rateCoApplicant3")
    def rateCoApplicant4 = column[Int]("rateCoApplicant4")
    def rateCoApplicant5 = column[Int]("rateCoApplicant5")
    def otherNativeRaceCoApplicant =
      column[String]("otherNativeRaceCoApplicant")
    def otherAsianRaceCoApplicant = column[String]("otherAsianRaceCoApplicant")
    def otherPacificRaceCoApplicant =
      column[String]("otherPacificRaceCoApplicant")
    def raceObservedApplicant = column[Int]("raceObservedApplicant")
    def raceObservedCoApplicant = column[Int]("raceObservedCoApplicant")
    def sexApplicant = column[Int]("sexApplicant")
    def sexCoApplicant = column[Int]("sexCoApplicant")
    def observedSexApplicant = column[Int]("observedSexApplicant")
    def observedSexCoApplicant = column[Int]("observedSexCoApplicant")
    def ageApplicant = column[Int]("ageApplicant")
    def ageCoApplicant = column[Int]("ageCoApplicant")
    def income = column[String]("income")
    def purchaserType = column[Int]("purchaserType")
    def rateSpread = column[String]("rateSpread")
    def hoepaStatus = column[Int]("hoepaStatus")
    def lienStatus = column[Int]("lienStatus")
    def creditScoreApplicant = column[Int]("creditScoreApplicant")
    def creditScoreCoApplicant = column[Int]("creditScoreCoApplicant")
    def creditScoreTypeApplicant = column[Int]("creditScoreTypeApplicant")
    def creditScoreModelApplicant = column[String]("creditScoreModelApplicant")
    def creditScoreTypeCoApplicant = column[Int]("creditScoreTypeCoApplicant")
    def creditScoreModelCoApplicant =
      column[String]("creditScoreModelCoApplicant")
    def denialReason1 = column[Int]("denialReason1")
    def denialReason2 = column[Int]("denialReason2")
    def denialReason3 = column[Int]("denialReason3")
    def denialReason4 = column[Int]("denialReason4")
    def otherDenialReason = column[String]("otherDenialReason")
    def totalLoanCosts = column[String]("totalLoanCosts")
    def totalPoints = column[String]("totalPoints")
    def originationCharges = column[String]("originationCharges")
    def discountPoints = column[String]("discountPoints")
    def lenderCredits = column[String]("lenderCredits")
    def interestRate = column[String]("interestRate")
    def paymentPenalty = column[String]("paymentPenalty")
    def debtToIncome = column[String]("debtToIncome")
    def loanValueRatio = column[String]("loanValueRatio")
    def loanTerm = column[String]("loanTerm")
    def rateSpreadIntro = column[String]("rateSpreadIntro")
    def baloonPayment = column[Int]("baloonPayment")
    def insertOnlyPayment = column[Int]("insertOnlyPayment")
    def amortization = column[Int]("amortization")
    def otherAmortization = column[Int]("otherAmortization")
    def propertyValues = column[String]("propertyValues")
    def homeSecurityPolicy = column[Int]("homeSecurityPolicy")
    def landPropertyInterest = column[Int]("landPropertyInterest")
    def totalUnits = column[Int]("totalUnits")
    def mfAffordable = column[String]("mfAffordable")
    def applicationSubmission = column[Int]("applicationSubmission")
    def payable = column[Int]("payable")
    def nmls = column[String]("nmls")
    def aus1 = column[Int]("aus1")
    def aus2 = column[Int]("aus2")
    def aus3 = column[Int]("aus3")
    def aus4 = column[Int]("aus4")
    def aus5 = column[Int]("aus5")
    def otheraus = column[String]("otheraus")
    def aus1Result = column[Int]("aus1Result")
    def aus2Result = column[Int]("aus2Result")
    def aus3Result = column[Int]("aus3Result")
    def aus4Result = column[Int]("aus4Result")
    def aus5Result = column[Int]("aus5Result")
    def otherAusResult = column[String]("otherAusResult")
    def reverseMortgage = column[Int]("reverseMortgage")
    def lineOfCredits = column[Int]("lineOfCredits")
    def businessOrCommercial = column[Int]("businessOrCommercial")

    def * =
      (larPartOneProjection,
       larPartTwoProjection,
       larPartThreeProjection,
       larPartFourProjection,
       larPartFiveProjection,
       larPartSixProjection) <> ((LarEntityImpl.apply _).tupled, LarEntityImpl.unapply)

    def larPartOneProjection =
      (id,
       lei,
       uli,
       appDate,
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
       tract) <> ((LarPartOne.apply _).tupled, LarPartOne.unapply)

    def larPartTwoProjection =
      (ethnicityApplicant1,
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
       raceApplicant5) <> ((LarPartTwo.apply _).tupled, LarPartTwo.unapply)

    def larPartThreeProjection =
      (otherNativeRaceApplicant,
       otherAsianRaceApplicant,
       otherPacificRaceApplicant,
       rateCoApplicant1,
       rateCoApplicant2,
       rateCoApplicant3,
       rateCoApplicant4,
       rateCoApplicant5,
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
       income) <> ((LarPartThree.apply _).tupled, LarPartThree.unapply)

    def larPartFourProjection =
      (purchaserType,
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
       originationCharges,
      ) <> ((LarPartFour.apply _).tupled, LarPartFour.unapply)

    def larPartFiveProjection =
      (discountPoints,
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
       propertyValues,
       homeSecurityPolicy,
       landPropertyInterest,
       totalUnits,
       mfAffordable,
       applicationSubmission) <> ((LarPartFive.apply _).tupled, LarPartFive.unapply)

    def larPartSixProjection =
      (payable,
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
       businessOrCommercial) <> ((LarPartSix.apply _).tupled, LarPartSix.unapply)

  }

  val larTable = TableQuery[LarTable]

  class LarRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[LarTable, String] {

    override val table: config.profile.api.TableQuery[LarTable] =
      larTable

    override def getId(row: LarTable): config.profile.api.Rep[Id] =
      row.lei

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)

    def insert(ts: LarEntityImpl): Future[Int] = {
      db.run(table += ts)
    }

    def findByLei(lei: String): Future[Seq[LarEntityImpl]] = {
      db.run(table.filter(_.lei === lei).result)
    }

    def deleteByLei(lei: String): Future[Int] = {
      db.run(table.filter(_.lei === lei).delete)
    }

    def count(): Future[Int] = {
      db.run(table.size.result)
    }

    def getAllLARs(): Future[Seq[LarEntityImpl]] = {
      db.run(table.result)
    }
  }

}
