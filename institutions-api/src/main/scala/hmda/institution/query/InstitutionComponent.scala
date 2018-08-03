package hmda.institution.query

import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait InstitutionComponent {

  import config.profile.api._

  object InstitutionsTable {
    implicit val stringListMapper = MappedColumnType.base[Seq[String], String](
      list => list.mkString(","),
      string => string.split(",").toList
    )
  }

  class InstitutionsTable(tag: Tag)
      extends Table[InstitutionEntity](tag, "institutions2018") {
    import InstitutionsTable._
    def lei = column[String]("lei", O.PrimaryKey)
    def activityYear = column[Int]("activity_year")
    def agency = column[Int]("agency")
    def institutionType = column[Int]("institution_type")
    def id2017 = column[String]("id2017")
    def taxId = column[String]("tax_id")
    def rssd = column[String]("rssd")
    def emailDomains = column[Seq[String]]("email_domains")
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

    def * =
      (lei,
       activityYear,
       agency,
       institutionType,
       id2017,
       taxId,
       rssd,
       emailDomains,
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

  class InstitutionEmailsTable(tag: Tag)
      extends Table[InstitutionEmailEntity](tag, "institutions_emails_2018") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lei = column[String]("lei")
    def email = column[String]("email")

    def * =
      (id, lei, email) <> (InstitutionEmailEntity.tupled, InstitutionEmailEntity.unapply)
  }

  class InstitutionRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[InstitutionsTable, String] {
    val table = TableQuery[InstitutionsTable]
    def getId(table: InstitutionsTable) = table.lei
    def deleteById(lei: String) = db.run(filterById(lei).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
  }

  class InstitutionEmailsRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[InstitutionEmailsTable, Int] {
    val table = TableQuery[InstitutionEmailsTable]
    def getId(table: InstitutionEmailsTable) = table.id
    def deleteById(id: Int) = db.run(filterById(id).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)

    def findByEmail(email: String) = {
      val emailDomain = extractDomain(email)
      val query = table.filter(_.email === emailDomain)
      db.run(query.result)
    }

    private def extractDomain(email: String): String = {
      val parts = email.toLowerCase.split("@")
      if (parts.length > 1)
        parts(1)
      else
        parts(0)
    }
  }

}
