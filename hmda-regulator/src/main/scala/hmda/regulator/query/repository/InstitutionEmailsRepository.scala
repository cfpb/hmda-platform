package hmda.regulator.query.repository

import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import hmda.regulator.query.panel.InstitutionEmailEntity
import hmda.regulator.query.repository.InstitutionsRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import dbConfig.profile.api._

trait InstitutionEmailsRepository extends InstitutionsRepository {

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

    def getAllDomains(): Future[Seq[InstitutionEmailEntity]] = {
      db.run(table.result)
    }
  }
}
