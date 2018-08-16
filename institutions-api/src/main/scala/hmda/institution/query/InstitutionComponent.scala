package hmda.institution.query

import hmda.institution.api.http.InstitutionConverter
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait InstitutionComponent {

  import dbConfig.profile.api._

  class InstitutionsTable(tag: Tag)
      extends Table[InstitutionEntity](tag, "institutions2018") {
    def lei = column[String]("lei", O.PrimaryKey)
    def activityYear = column[Int]("activity_year")
    def agency = column[Int]("agency")
    def institutionType = column[Int]("institution_type")
    def id2017 = column[String]("id2017")
    def taxId = column[String]("tax_id")
    def rssd = column[String]("rssd")
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

  class InstitutionRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[InstitutionsTable, String] {
    val table = institutionsTable
    def getId(table: InstitutionsTable) = table.lei
    def deleteById(lei: String) = db.run(filterById(lei).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
  }

  class InstitutionEmailsRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[InstitutionEmailsTable, Int] {
    val table = institutionEmailsTable
    def getId(table: InstitutionEmailsTable) = table.id
    def deleteById(id: Int) = db.run(filterById(id).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)

    def findByEmail(email: String) = {
      val emailDomain = extractDomain(email)
      val query = table.filter(_.emailDomain === emailDomain)
      db.run(query.result)
    }

    def findByLei(lei: String) = {
      db.run(table.filter(_.lei === lei).result)
    }
  }

  def findByEmail(email: String)(
      implicit ec: ExecutionContext,
      institutionRepository: InstitutionRepository,
      institutionEmailsRepository: InstitutionEmailsRepository)
    : Future[Seq[Institution]] = {

    val db = institutionRepository.db
    val emailDomain = extractDomain(email)
    val emailQuery =
      institutionEmailsRepository.table.filter(_.emailDomain === emailDomain)

    def institutionQuery(leis: Seq[String]) =
      institutionRepository.table.filter(_.lei inSet leis)

    for {
      emailEntities <- db.run(emailQuery.result)
      leis = emailEntities.map(_.lei)
      institutions <- db.run(institutionQuery(leis).result)
    } yield {
      institutions.map(
        institution =>
          InstitutionConverter.convert(institution,
                                       emailEntities.map(_.emailDomain)))
    }

  }

  private def extractDomain(email: String): String = {
    val parts = email.toLowerCase.split("@")
    if (parts.length > 1)
      parts(1)
    else
      parts(0)
  }
}
