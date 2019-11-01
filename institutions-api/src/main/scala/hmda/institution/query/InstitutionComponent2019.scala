package hmda.institution.query

import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait InstitutionComponent2019 {

  import dbConfig.profile.api._

  class InstitutionsTable2019(tag: Tag, tableName: String) extends Table[InstitutionEntity](tag, tableName) {
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
    def assets          = column[Int]("assets")
    def otherLenderCode = column[Int]("other_lender_code")
    def topHolderIdRssd = column[Int]("topholder_id_rssd")
    def topHolderName   = column[String]("topholder_name")
    def hmdaFiler       = column[Boolean]("hmda_filer")
    def quarterlyFiler  = column[Boolean]("quarterly_filer")

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
        false
      ) <> (InstitutionEntity.tupled, InstitutionEntity.unapply)
  }

  val institutionsTable2019 = TableQuery[InstitutionsTable2019]((tag: Tag) => new InstitutionsTable2019(tag, "institutions2019"))

  class InstitutionRepository2019(val config: DatabaseConfig[JdbcProfile], tableName: String)
      extends TableRepository[InstitutionsTable2019, String] {
    val institutionsTable2019               = TableQuery[InstitutionsTable2019]((tag: Tag) => new InstitutionsTable2019(tag, tableName))
    val table                               = institutionsTable2019
    def getId(table: InstitutionsTable2019) = table.lei
    def deleteById(lei: String)             = db.run(filterById(lei).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)
  }

  class InstitutionRepository2019Beta(val config: DatabaseConfig[JdbcProfile], tableName: String)
      extends TableRepository[InstitutionsTable2019, String] {
    val institutionsTable2019Beta = TableQuery[InstitutionsTable2019]((tag: Tag) => new InstitutionsTable2019(tag, tableName))
    val table                     = institutionsTable2019Beta

    def getId(table: InstitutionsTable2019) = table.lei
    def deleteById(lei: String)             = db.run(filterById(lei).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)
  }

}
