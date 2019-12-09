package hmda.institution.query

import hmda.institution.api.http.InstitutionConverter
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait InstitutionEmailComponent extends InstitutionComponent2018 with InstitutionComponent2019 with InstitutionComponent2020 {

  import dbConfig.profile.api._

  class InstitutionEmailsTable(tag: Tag) extends Table[InstitutionEmailEntity](tag, "institutions_emails_2018") {
    def id          = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lei         = column[String]("lei")
    def emailDomain = column[String]("email_domain")

    def * =
      (id, lei, emailDomain) <> (InstitutionEmailEntity.tupled, InstitutionEmailEntity.unapply)
  }

  val institutionEmailsTable = TableQuery[InstitutionEmailsTable]

  class InstitutionEmailsRepository(val config: DatabaseConfig[JdbcProfile]) extends TableRepository[InstitutionEmailsTable, Int] {
    val table                                = institutionEmailsTable
    def getId(table: InstitutionEmailsTable) = table.id
    def deleteById(id: Int)                  = db.run(filterById(id).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)

    def findByEmail(email: String) = {
      val emailDomain = extractDomain(email)
      val query       = table.filter(_.emailDomain.trim === emailDomain.trim)
      db.run(query.result)
    }

    def findByLei(lei: String) =
      db.run(table.filter(_.lei === lei).result)
  }

  def updateEmails(
    i: InstitutionEmailEntity
  )(implicit ec: ExecutionContext, institutionEmailsRepository: InstitutionEmailsRepository): Future[Int] = {
    val db    = institutionEmailsRepository.db
    val table = institutionEmailsRepository.table
    for {
      query <- db.run(table.filter(_.lei === i.lei).result)
      result <- if (!query.toList.map(_.emailDomain).contains(i.emailDomain)) {
                 db.run(table += i)
               } else Future.successful(0)
    } yield {
      result
    }
  }

  def deleteEmails(lei: String)(implicit ec: ExecutionContext, institutionEmailsRepository: InstitutionEmailsRepository): Future[Int] = {
    val db    = institutionEmailsRepository.db
    val table = institutionEmailsRepository.table
    for {
      query <- db.run(table.filter(_.lei === lei).delete)
    } yield {
      query
    }
  }

  def findByEmail(email: String, year: String)(
    implicit ec: ExecutionContext,
    institutionEmailsRepository: InstitutionEmailsRepository,
    institutionRepository2018: InstitutionRepository2018,
    institutionRepository2019: InstitutionRepository2019,
    institutionRepository2020: InstitutionRepository2020
  ): Future[Seq[Institution]] = {

    val emailDomain = extractDomain(email)
    val emailSingleQuery =
      institutionEmailsRepository.table.filter(_.emailDomain.trim === emailDomain.trim)

    def emailTotalQuery(leis: Seq[String]) =
      institutionEmailsRepository.table.filter(_.lei inSet leis)

    year match {
      case "2018" =>
        def institutionQuery(leis: Seq[String]) =
          institutionRepository2018.table.filter(_.lei inSet leis)

        val db = institutionRepository2018.db

        for {
          emailEntities <- db.run(emailSingleQuery.result)
          leis          = emailEntities.map(_.lei)
          institutions  <- db.run(institutionQuery(leis).result)
          emails        <- db.run(emailTotalQuery(leis).result)
        } yield {
          institutions.map { institution =>
            val filteredEmails =
              emails.filter(_.lei == institution.lei).map(_.emailDomain)
            InstitutionConverter.convert(institution, filteredEmails)
          }
        }
      case "2019" =>
        def institutionQuery(leis: Seq[String]) =
          institutionRepository2019.table.filter(_.lei inSet leis)

        val db = institutionRepository2019.db

        for {
          emailEntities <- db.run(emailSingleQuery.result)
          leis          = emailEntities.map(_.lei)
          institutions  <- db.run(institutionQuery(leis).result)
          emails        <- db.run(emailTotalQuery(leis).result)
        } yield {
          institutions.map { institution =>
            val filteredEmails =
              emails.filter(_.lei == institution.lei).map(_.emailDomain)
            InstitutionConverter.convert(institution, filteredEmails)
          }
        }
      case "2020" =>
        def institutionQuery(leis: Seq[String]) =
          institutionRepository2020.table.filter(_.lei inSet leis)

        val db = institutionRepository2020.db

        for {
          emailEntities <- db.run(emailSingleQuery.result)
          leis          = emailEntities.map(_.lei)
          institutions  <- db.run(institutionQuery(leis).result)
          emails        <- db.run(emailTotalQuery(leis).result)
        } yield {
          institutions.map { institution =>
            val filteredEmails =
              emails.filter(_.lei == institution.lei).map(_.emailDomain)
            InstitutionConverter.convert(institution, filteredEmails)
          }
        }
    }
  }

  def findByFields(lei: String, name: String, taxId: String, emailDomain: String, year: String)(
    implicit ec: ExecutionContext,
    institutionEmailsRepository: InstitutionEmailsRepository,
    institutionRepository2018: InstitutionRepository2018,
    institutionRepository2019: InstitutionRepository2019,
    institutionRepository2020: InstitutionRepository2020
  ): Future[Seq[Institution]] = {
    val emailFiltered = findByEmail(emailDomain, year)
    for {
      emailEntities <- emailFiltered
      filtered = emailEntities.filter(
        i =>
          i.LEI == lei && i.respondent.name
            .getOrElse("") == name && i.taxId.getOrElse("") == taxId
      )
    } yield filtered
  }

  private def extractDomain(email: String): String = {
    val parts = email.toLowerCase.split("@")
    if (parts.length > 1)
      parts(1)
    else
      parts(0)
  }
}
