package hmda.query.repository.institutions

import hmda.query.DbConfiguration._
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._
import slick.jdbc.JdbcProfile

trait InstitutionComponent {
  import config.profile.api._

  class InstitutionsTable(tag: Tag) extends Table[InstitutionQuery](tag, "institutions") {
    def id = column[String]("id", O.PrimaryKey)
    def agency = column[Int]("agency")
    def filingPeriod = column[Int]("period")
    def activityYear = column[Int]("activity_year")
    def respondentId = column[String]("respondent_id")
    def institutionType = column[String]("type")
    def cra = column[Boolean]("cra")

    def emailDomain1 = column[String]("email_1")
    def emailDomain2 = column[String]("email_2")
    def emailDomain3 = column[String]("email_3")

    def respondentName = column[String]("respondent_name")
    def respondentState = column[String]("respondent_state")
    def respondentCity = column[String]("respondent_city")
    def respondentFips = column[String]("respondent_fips")

    def hmdaFiler = column[Boolean]("hmda_filer")

    def parentRespondentId = column[String]("parent_respondent_id")
    def parentIdRssd = column[Int]("parent_id_rssd")
    def parentName = column[String]("parent_name")
    def parentCity = column[String]("parent_city")
    def parentState = column[String]("parent_state")

    def assets = column[Int]("assets")
    def otherLenderCodes = column[Int]("other_lender_codes")

    def topHolderIdRssd = column[Int]("top_holder_id_rssd")
    def topHolderName = column[String]("top_holder_name")
    def topHolderCity = column[String]("top_holder_city")
    def topHolderState = column[String]("top_holder_state")
    def topHolderCountry = column[String]("top_holder_country")

    type InstitutionQueryHList = String :: Int :: Int :: Int :: String :: String :: Boolean :: String :: String :: String :: String :: String :: String :: String :: Boolean :: String :: Int :: String :: String :: String :: Int :: Int :: Int :: String :: String :: String :: String :: HNil

    def createInstitutionQuery(data: InstitutionQueryHList): InstitutionQuery = data match {
      case id ::
        agency ::
        filingPeriod ::
        activityYear ::
        respondentId ::
        institutionType ::
        cra ::
        emailDomain1 ::
        emailDomain2 ::
        emailDomain3 ::
        respondentName ::
        respondentState ::
        respondentCity ::
        respondentFips ::
        hmdaFiler ::
        parentRespondentId ::
        parentIdRssd ::
        parentName ::
        parentCity ::
        parentState ::
        assets ::
        otherLenderCodes ::
        topHolderIdRssd ::
        topHolderName ::
        topHolderCity ::
        topHolderState ::
        topHolderCountry ::
        HNil =>
        InstitutionQuery(
          id,
          agency,
          filingPeriod,
          activityYear,
          respondentId,
          institutionType,
          cra,
          emailDomain1,
          emailDomain2,
          emailDomain3,
          respondentName,
          respondentState,
          respondentCity,
          respondentFips,
          hmdaFiler,
          parentRespondentId,
          parentIdRssd,
          parentName,
          parentCity,
          parentState,
          assets,
          otherLenderCodes,
          topHolderIdRssd,
          topHolderName,
          topHolderCity,
          topHolderState,
          topHolderCountry
        )
    }

    def extractInstitutionQuery(data: InstitutionQuery): Option[InstitutionQueryHList] = data match {
      case InstitutionQuery(
        id,
        agency,
        filingPeriod,
        activityYear,
        respondentId,
        institutionType,
        cra,
        emailDomain1,
        emailDomain2,
        emailDomain3,
        respondentName,
        respondentState,
        respondentCity,
        respondentFips,
        hmdaFiler,
        parentRespondentId,
        parentIdRssd,
        parentName,
        parentCity,
        parentState,
        assets,
        otherLenderCodes,
        topHolderIdRssd,
        topHolderName,
        topHolderCity,
        topHolderState,
        topHolderCountry
        ) =>
        Some(
          id ::
            agency ::
            filingPeriod ::
            activityYear ::
            respondentId ::
            institutionType ::
            cra ::
            emailDomain1 ::
            emailDomain2 ::
            emailDomain3 ::
            respondentName ::
            respondentState ::
            respondentCity ::
            respondentFips ::
            hmdaFiler ::
            parentRespondentId ::
            parentIdRssd ::
            parentName ::
            parentCity ::
            parentState ::
            assets ::
            otherLenderCodes ::
            topHolderIdRssd ::
            topHolderName ::
            topHolderCity ::
            topHolderState ::
            topHolderCountry ::
            HNil
        )
    }

    override def * = (
      id ::
      agency ::
      filingPeriod ::
      activityYear ::
      respondentId ::
      institutionType ::
      cra ::
      emailDomain1 ::
      emailDomain2 ::
      emailDomain3 ::
      respondentName ::
      respondentState ::
      respondentCity ::
      respondentFips ::
      hmdaFiler ::
      parentRespondentId ::
      parentIdRssd ::
      parentName ::
      parentCity ::
      parentState ::
      assets ::
      otherLenderCodes ::
      topHolderIdRssd ::
      topHolderName ::
      topHolderCity ::
      topHolderState ::
      topHolderCountry ::
      HNil
    ) <> (createInstitutionQuery, extractInstitutionQuery)
  }

  class InstitutionRepository(val config: DatabaseConfig[JdbcProfile]) extends TableRepository[InstitutionsTable, String] {
    val table = TableQuery[InstitutionsTable]
    def getId(table: InstitutionsTable) = table.id

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
    def deleteById(id: String) = db.run(filterById(id).delete)
  }

}
