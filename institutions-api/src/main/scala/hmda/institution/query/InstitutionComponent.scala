package hmda.institution.query

import com.typesafe.config.{Config, ConfigFactory}
import hmda.institution.query.InstitutionTsRepo.selectTransmittalSheet
import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import hmda.query.ts.TransmittalSheetRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait InstitutionComponent {

  import dbConfig.profile.api._

  class InstitutionsTable(tag: Tag, tableName: String) extends Table[InstitutionEntity](tag, tableName) {
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
    def quarterlyFiler  = column[Boolean]("quarterly_filer")
    def quarterlyFilerHasFiledQ1  = column[Boolean]("quarterly_filer_has_filed_q1")
    def quarterlyFilerHasFiledQ2  = column[Boolean]("quarterly_filer_has_filed_q2")
    def quarterlyFilerHasFiledQ3  = column[Boolean]("quarterly_filer_has_filed_q3")
    def notes                     = column[String]("notes")

    def * =
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
        hmdaFiler,
        quarterlyFiler,
        quarterlyFilerHasFiledQ1,
        quarterlyFilerHasFiledQ2,
        quarterlyFilerHasFiledQ3,
        notes
      ) <> (InstitutionEntity.tupled, InstitutionEntity.unapply)
  }

  class InstitutionRepository(val config: DatabaseConfig[JdbcProfile], tableName: String)
      extends TableRepository[InstitutionsTable, String] {
    val table                           = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, tableName))
    def getId(table: InstitutionsTable) = table.lei
    def deleteById(lei: String)         = db.run(filterById(lei).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)
  }

  val institutionConfig: Config = ConfigFactory.load().getConfig("hmda.institution")

  val yearsAvailable: Seq[String] = institutionConfig.getString("yearsAvailable").split(",").toSeq

  val institutionRepositories: Map[String, InstitutionRepository] = (for {
    year <- yearsAvailable
  } yield year -> new InstitutionRepository(dbConfig, s"institutions$year")).toMap

  val tsRepositories: Map[String, TransmittalSheetRepository] = (for {
    year <- yearsAvailable
  } yield year -> new TransmittalSheetRepository(dbConfig, selectTransmittalSheet(year.toInt))).toMap

}
