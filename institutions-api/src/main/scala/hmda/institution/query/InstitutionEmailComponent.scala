package hmda.institution.query

import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionConverter
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait InstitutionEmailComponent extends InstitutionComponent {

  import dbConfig.profile.api._

  class InstitutionEmailsTable(tag: Tag) extends Table[InstitutionEmailEntity](tag, "institutions_emails_2018") {
    def id          = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lei         = column[String]("lei")
    def emailDomain = column[String]("email_domain")

    def * =
      (id, lei, emailDomain) <> (InstitutionEmailEntity.tupled, InstitutionEmailEntity.unapply)
  }

  val institutionEmailsTable = TableQuery[InstitutionEmailsTable]

  val bankFilter = ConfigFactory.load("application.conf").getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")


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

  def findByYear(year: String)(
    implicit ec: ExecutionContext,
    institutionEmailsRepository: InstitutionEmailsRepository,
    institutionRepository2018: InstitutionRepository,
    institutionRepository2019: InstitutionRepository,
    institutionRepository2020: InstitutionRepository,
    institutionRepository2021: InstitutionRepository,
    institutionRepository2022: InstitutionRepository

  ):  Future[Seq[Future[String]]]= {

    year match {
      case "2018" =>
        def institutionQuery() =
          institutionRepository2018.table.filterNot(_.lei.toUpperCase inSet bankFilterList)
        val db = institutionRepository2018.db

        for {
          institutions  <- db.run(institutionQuery().result)
        } yield {
          institutions.map { institution => appendLoaderEmailDomains(institution)}
        }
      case "2019" =>
        def institutionQuery() =
          institutionRepository2019.table.filterNot(_.lei.toUpperCase inSet bankFilterList)
        val db = institutionRepository2019.db

        for {
          institutions  <- db.run(institutionQuery().result)
        } yield {
          institutions.map { institution => appendLoaderEmailDomains(institution)}
        }
      case "2020" =>
        def institutionQuery() =
          institutionRepository2020.table.filterNot(_.lei.toUpperCase inSet bankFilterList)
        val db = institutionRepository2020.db

        for {
          institutions  <- db.run(institutionQuery().result)
        } yield {

          institutions.map(institution => appendLoaderEmailDomains(institution))

        }
      case "2021" =>
        def institutionQuery() =
          institutionRepository2021.table.filterNot(_.lei.toUpperCase inSet bankFilterList)
        val db = institutionRepository2021.db

        for {
          institutions  <- db.run(institutionQuery().result)
        } yield {

          institutions.map(institution => appendLoaderEmailDomains(institution))

        }

      case "2022" =>
        def institutionQuery() =
          institutionRepository2022.table.filterNot(_.lei.toUpperCase inSet bankFilterList)
        val db = institutionRepository2022.db

        for {
          institutions  <- db.run(institutionQuery().result)
        } yield {

          institutions.map(institution => appendLoaderEmailDomains(institution))

        }
    }
  }

  def appendLoaderEmailDomains(
   institution: InstitutionEntity)( implicit ec: ExecutionContext,
                                    institutionEmailsRepository: InstitutionEmailsRepository):Future[String]= {
    val fEmails = institutionEmailsRepository.findByLei(institution.lei)

   for {
      institution <- fEmails.map(emails => mergeEmailIntoInstitutions(emails,institution).toLoaderPSV)
    }
     yield institution

  }



  def findByEmail(email: String, year: String, 
    institutionRepository2018: InstitutionRepository,
    institutionRepository2019: InstitutionRepository,
    institutionRepository2020: InstitutionRepository,
    institutionRepository2021: InstitutionRepository,
    institutionRepository2022: InstitutionRepository)(
    implicit ec: ExecutionContext,
    institutionEmailsRepository: InstitutionEmailsRepository
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
          institutions.map { institution => mergeEmailIntoInstitutions(emails, institution)
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
          institutions.map { institution => mergeEmailIntoInstitutions(emails, institution)
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
          institutions.map { institution => mergeEmailIntoInstitutions(emails, institution)
          }
        }
      case "2021" =>
        def institutionQuery(leis: Seq[String]) =
          institutionRepository2021.table.filter(_.lei inSet leis)

        val db = institutionRepository2021.db

        for {
          emailEntities <- db.run(emailSingleQuery.result)
          leis          = emailEntities.map(_.lei)
          institutions  <- db.run(institutionQuery(leis).result)
          emails        <- db.run(emailTotalQuery(leis).result)
        } yield {
          institutions.map { institution => mergeEmailIntoInstitutions(emails, institution)
          }
        }
      case "2022" =>
        def institutionQuery(leis: Seq[String]) =
          institutionRepository2022.table.filter(_.lei inSet leis)

        val db = institutionRepository2022.db

        for {
          emailEntities <- db.run(emailSingleQuery.result)
          leis          = emailEntities.map(_.lei)
          institutions  <- db.run(institutionQuery(leis).result)
          emails        <- db.run(emailTotalQuery(leis).result)
        } yield {
          institutions.map { institution => mergeEmailIntoInstitutions(emails, institution)
          }
        }
    }
  }

  def findByEmailAnyYear(email: String,     
    institutionRepository2018: InstitutionRepository,
    institutionRepository2019: InstitutionRepository,
    institutionRepository2020: InstitutionRepository,
    institutionRepository2021: InstitutionRepository,
    institutionRepository2022: InstitutionRepository)(
    implicit ec: ExecutionContext,
    institutionEmailsRepository: InstitutionEmailsRepository
  ): Future[Seq[Institution]] = {

      for {
        institutions2022 <- findByEmail(email, "2022", institutionRepository2018, institutionRepository2019, institutionRepository2020, institutionRepository2021, institutionRepository2022)
        institutions2021 <- findByEmail(email, "2021", institutionRepository2018, institutionRepository2019, institutionRepository2020, institutionRepository2021, institutionRepository2022)
        institutions2020 <- findByEmail(email, "2020", institutionRepository2018, institutionRepository2019, institutionRepository2020, institutionRepository2021, institutionRepository2022)
        institutions2019 <- findByEmail(email, "2019", institutionRepository2018, institutionRepository2019, institutionRepository2020, institutionRepository2021, institutionRepository2022)
        institutions2018 <- findByEmail(email, "2018", institutionRepository2018, institutionRepository2019, institutionRepository2020, institutionRepository2021, institutionRepository2022)
        }
      yield (institutions2021, institutions2020, institutions2019, institutions2018, institutions2022) match {
        case _ if (!institutions2021.isEmpty) => institutions2021
        case _ if (!institutions2020.isEmpty) => institutions2020
        case _ if (!institutions2019.isEmpty) => institutions2019
        case _ if (!institutions2018.isEmpty) => institutions2018
        case _ if (!institutions2022.isEmpty) => institutions2022
      }

  }

  private def mergeEmailIntoInstitutions(emails: Seq[InstitutionEmailEntity], institution: InstitutionEntity) = {
    val filteredEmails =
      emails.filter(_.lei == institution.lei).map(_.emailDomain)
    InstitutionConverter.convert(institution, filteredEmails)
  }

  def findByFields(lei: String, name: String, taxId: String, emailDomain: String, year: String,
    institutionRepository2018: InstitutionRepository,
    institutionRepository2019: InstitutionRepository,
    institutionRepository2020: InstitutionRepository,
    institutionRepository2021: InstitutionRepository,
    institutionRepository2022: InstitutionRepository
    )(
    implicit ec: ExecutionContext,
    institutionEmailsRepository: InstitutionEmailsRepository
  ): Future[Seq[Institution]] = {
    val emailFiltered = findByEmail(emailDomain, year, institutionRepository2018, institutionRepository2019, institutionRepository2020, institutionRepository2021, institutionRepository2022)
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
