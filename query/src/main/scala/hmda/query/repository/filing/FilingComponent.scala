package hmda.query.repository.filing

import hmda.query.DbConfiguration
import hmda.query.model.filing.{ LoanApplicationRegisterQuery, LoanApplicationRegisterTotal }
import hmda.query.repository.{ Repository, TableRepository }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

trait FilingComponent { this: DbConfiguration =>
  import config.profile.api._

  class LarTable(tag: Tag) extends Table[LoanApplicationRegisterQuery](tag, "lars") {
    def id = column[String]("id", O.PrimaryKey)

    def respondentId = column[String]("respondent_id")
    def agencyCode = column[Int]("agency_code")
    def preapprovals = column[Int]("preapprovals")
    def actionTakenType = column[Int]("action_taken_type")
    def actionTakenDate = column[Int]("action_taken_date")
    def purchaserType = column[Int]("purchaser_type")
    def rateSpread = column[String]("rate_spread")
    def hoepaStatus = column[Int]("hoepa_status")
    def lienStatus = column[Int]("lien_status")

    def loanId = column[String]("loan_id")
    def applicationDate = column[String]("application_date")
    def loanType = column[Int]("loan_type")
    def propertyType = column[Int]("property_type")
    def purpose = column[Int]("purpose")
    def occupancy = column[Int]("occupancy")
    def amount = column[Int]("amount")

    def msa = column[String]("msa")
    def state = column[String]("state")
    def county = column[String]("county")
    def tract = column[String]("tract")

    def ethnicity = column[Int]("ethnicity")
    def coEthnicity = column[Int]("co_ethnicity")
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

    def denialReason1 = column[String]("denial_reason1")
    def denialReason2 = column[String]("denial_reason2")
    def denialReason3 = column[String]("denial_reason3")
    def period = column[String]("period")

    type LarQueryHList = String :: String :: Int :: Int :: Int :: Int :: Int :: String :: Int :: Int :: String :: String :: Int :: Int :: Int :: Int :: Int :: String :: String :: String :: String :: Int :: Int :: Int :: String :: String :: String :: String :: Int :: String :: String :: String :: String :: Int :: Int :: String :: String :: String :: String :: String :: HNil

    def createLarQuery(data: LarQueryHList): LoanApplicationRegisterQuery = data match {
      case id ::
        respondentId ::
        agencyCode ::
        preapprovals ::
        actionTakenType ::
        actionTakenDate ::
        purchaserType ::
        rateSpread ::
        hoepaStatus ::
        lienStatus ::
        loanId ::
        applicationDate ::
        loanType ::
        propertyType ::
        purpose ::
        occupancy ::
        amount ::
        msa ::
        state ::
        county ::
        tract ::
        ethnicity ::
        coEthnicity ::
        race1 ::
        race2 ::
        race3 ::
        race4 ::
        race5 ::
        coRace1 ::
        coRace2 ::
        coRace3 ::
        coRace4 ::
        coRace5 ::
        sex ::
        coSex ::
        income ::
        denialReason1 ::
        denialReason2 ::
        denialReason3 ::
        period ::
        HNil =>
        LoanApplicationRegisterQuery(
          id,
          respondentId,
          agencyCode,
          preapprovals,
          actionTakenType,
          actionTakenDate,
          purchaserType,
          rateSpread,
          hoepaStatus,
          lienStatus,
          loanId,
          applicationDate,
          loanType,
          propertyType,
          purpose,
          occupancy,
          amount,
          msa,
          state,
          county,
          tract,
          ethnicity,
          coEthnicity,
          race1,
          race2,
          race3,
          race4,
          race5,
          coRace1,
          coRace2,
          coRace3,
          coRace4,
          coRace5,
          sex,
          coSex,
          income,
          denialReason1,
          denialReason2,
          denialReason3,
          period
        )
    }

    def extractLarQuery(data: LoanApplicationRegisterQuery): Option[LarQueryHList] = data match {
      case LoanApplicationRegisterQuery(
        id,
        respondentId,
        agencyCode,
        preapprovals,
        actionTakenType,
        actionTakenDate,
        purchaserType,
        rateSpread,
        hoepaStatus,
        lienStatus,
        loanId,
        applicationDate,
        loanType,
        propertyType,
        purpose,
        occupancy,
        amount,
        msa,
        state,
        county,
        tract,
        ethnicity,
        coEthnicity,
        race1,
        race2,
        race3,
        race4,
        race5,
        coRace1,
        coRace2,
        coRace3,
        coRace4,
        coRace5,
        sex,
        coSex,
        income,
        denialReason1,
        denialReason2,
        denialReason3,
        period
        ) =>
        Some(
          id ::
            respondentId ::
            agencyCode ::
            preapprovals ::
            actionTakenType ::
            actionTakenDate ::
            purchaserType ::
            rateSpread ::
            hoepaStatus ::
            lienStatus ::
            loanId ::
            applicationDate ::
            loanType ::
            propertyType ::
            purpose ::
            occupancy ::
            amount ::
            msa ::
            state ::
            county ::
            tract ::
            ethnicity ::
            coEthnicity ::
            race1 ::
            race2 ::
            race3 ::
            race4 ::
            race5 ::
            coRace1 ::
            coRace2 ::
            coRace3 ::
            coRace4 ::
            coRace5 ::
            sex ::
            coSex ::
            income ::
            denialReason1 ::
            denialReason2 ::
            denialReason3 ::
            period ::
            HNil
        )
    }

    def * = (
      id ::
      respondentId ::
      agencyCode ::
      preapprovals ::
      actionTakenType ::
      actionTakenDate ::
      purchaserType ::
      rateSpread ::
      hoepaStatus ::
      lienStatus ::
      loanId ::
      applicationDate ::
      loanType ::
      propertyType ::
      purpose ::
      occupancy ::
      amount ::
      msa ::
      state ::
      county ::
      tract ::
      ethnicity ::
      coEthnicity ::
      race1 ::
      race2 ::
      race3 ::
      race4 ::
      race5 ::
      coRace1 ::
      coRace2 ::
      coRace3 ::
      coRace4 ::
      coRace5 ::
      sex ::
      coSex ::
      income ::
      denialReason1 ::
      denialReason2 ::
      denialReason3 ::
      period ::
      HNil
    ) <> (createLarQuery, extractLarQuery)

  }

  class LarRepository(val config: DatabaseConfig[JdbcProfile]) extends TableRepository[LarTable, String] {
    val table = TableQuery[LarTable]
    def getId(table: LarTable) = table.id

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
    def deleteById(id: String) = db.run(filterById(id).delete)
    def deleteAll = db.run(table.delete)
    def deleteByRespondentId(respId: String) = db.run(table.filter(_.respondentId === respId).delete)
  }

  class LarTotalTable(tag: Tag) extends Table[LoanApplicationRegisterTotal](tag, "lars_total") {
    def id = column[Int]("id", O.PrimaryKey)
    def respondentId = column[String]("respondent_id")
    def period = column[String]("period")
    def total = column[Int]("total")

    override def * = (id, respondentId, period, total) <> (LoanApplicationRegisterTotal.tupled, LoanApplicationRegisterTotal.unapply)
  }

  class LarTotalRepository(val config: DatabaseConfig[JdbcProfile]) extends Repository[LarTotalTable, Int] {
    val table = TableQuery[LarTotalTable]
    def getId(table: LarTotalTable) = table.id

    private val createViewSchema = sqlu"""create view lars_total as
      select count(*) as total, respondent_id, period
      from lars group by respondent_id, period
      """

    def createSchema() = db.run(createViewSchema)
    def dropSchema() = db.run(table.schema.drop)
  }

}
