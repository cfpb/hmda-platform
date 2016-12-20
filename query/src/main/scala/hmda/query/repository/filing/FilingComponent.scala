package hmda.query.repository.filing

import hmda.model.fi.lar.{ Denial, Loan, Geography, Applicant }
import hmda.query.DbConfiguration
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.Repository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait FilingComponent { this: DbConfiguration =>
  import config.profile.api._

  class LarTable(tag: Tag) extends Table[LoanApplicationRegisterQuery](tag, "lars") {
    def id = column[String]("id", O.PrimaryKey)
    def respondentId = column[String]("respondent_id")
    def agencyCode = column[Int]("agency_code")

    def loan = (loanId, applicationDate, loanType, propertyType, purpose, occupancy, amount)
    def loanId = column[String]("loan_id")
    def applicationDate = column[String]("application_date")
    def loanType = column[Int]("loan_type")
    def propertyType = column[Int]("property_type")
    def purpose = column[Int]("purpose")
    def occupancy = column[Int]("occupancy")
    def amount = column[Int]("amount")

    def preapprovals = column[Int]("preapprovals")
    def actionTakenType = column[Int]("action_taken_type")
    def actionTakenDate = column[Int]("action_taken_date")

    def geography = (msa, state, county, tract)
    def msa = column[String]("msa")
    def state = column[String]("state")
    def county = column[String]("county")
    def tract = column[String]("tract")

    def applicant = (ethnicity, coEthnicity, race1, race2, race3, race4, race5, coRace1, coRace2, coRace3, coRace4, coRace5, sex, coSex, income)
    def ethnicity = column[Int]("ethnicity")
    def coEthnicity = column[Int]("co-ethnicity")
    def race1 = column[Int]("race1")
    def race2 = column[String]("race2")
    def race3 = column[String]("race3")
    def race4 = column[String]("race4")
    def race5 = column[String]("race5")
    def coRace1 = column[Int]("co_race1")
    def coRace2 = column[String]("co_race2")
    def coRace3 = column[String]("co_race3")
    def coRace4 = column[String]("co_race4")
    def coRace5 = column[String]("co_race5")
    def sex = column[Int]("sex")
    def coSex = column[Int]("co_sex")
    def income = column[String]("income")

    def purchaserType = column[Int]("purchaser_type")

    def denial = (denialReason1, denialReason2, denialReason3)
    def denialReason1 = column[String]("denial_reason1")
    def denialReason2 = column[String]("denial_reason2")
    def denialReason3 = column[String]("denial_reason3")

    def rateSpread = column[String]("rate_spread")
    def hoepaStatus = column[Int]("hoepa_status")
    def lienStatus = column[Int]("lien_status")

    //val standaloneFieldsProjection = standaloneFields <> (standaloneFields.tupled, standaloneFields.unapply)
    val loanProjection = loan <> (Loan.tupled, Loan.unapply)
    val geographyProjection = geography <> (Geography.tupled, Geography.unapply)
    val applicantProjection = applicant <> (Applicant.tupled, Applicant.unapply)
    val denialProjection = denial <> (Denial.tupled, Denial.unapply)

    override def * = (
      id,
      respondentId,
      agencyCode,
      loanProjection,
      preapprovals,
      actionTakenType,
      actionTakenDate,
      geographyProjection,
      applicantProjection,
      purchaserType,
      denialProjection,
      rateSpread,
      hoepaStatus,
      lienStatus
    ).shaped <> (
        //Map from tuple to case class
        {
          case (id, respondentId, agencyCode, loan, preapprovals, actionTakenType, actionTakenDate, geography, applicant, purchaserType, denial, rateSpread, hoepaStatus, lienStatus) => LoanApplicationRegisterQuery(
            id,
            respondentId,
            agencyCode,
            loan,
            preapprovals,
            actionTakenType,
            actionTakenDate,
            geography,
            applicant,
            purchaserType,
            denial,
            rateSpread,
            hoepaStatus,
            lienStatus
          )
        },
        //Map from case class to tuple
        (lar: LoanApplicationRegisterQuery) => Some(
          lar.id,
          lar.respondentId,
          lar.agencyCode,
          lar.loan,
          preapprovals,
          actionTakenType,
          actionTakenDate,
          lar.geography,
          lar.applicant,
          purchaserType,
          lar.denial,
          rateSpread,
          hoepaStatus,
          lienStatus
        )
      )
  }

  class LarRepository(val config: DatabaseConfig[JdbcProfile]) extends Repository[LarTable, String] {
    val table = TableQuery[LarTable]
    def getId(table: LarTable) = table.id

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
    def deleteById(id: String) = db.run(filterById(id).delete)
  }
}
