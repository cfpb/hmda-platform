package hmda.publisher.query.component

import hmda.publisher.helper.PGTableNameLoader
import hmda.publisher.query.panel.InstitutionEmailEntity
import hmda.query.DbConfiguration.dbConfig
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait InstitutionEmailComponent extends PGTableNameLoader{


  import dbConfig.profile.api._

  class InstitutionEmailsTable(tag: Tag)
    extends Table[InstitutionEmailEntity](tag, emailTableName) with InstitutionEmailComponent {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lei = column[String]("lei")
    def emailDomain = column[String]("email_domain")

    def * =
      (id, lei, emailDomain) <> (InstitutionEmailEntity.tupled, InstitutionEmailEntity.unapply)

  }

  val institutionEmailsTable2018 = TableQuery[InstitutionEmailsTable]

  class InstitutionEmailsRepository2018(val config: DatabaseConfig[JdbcProfile])
    extends TableRepository[InstitutionEmailsTable, Int] {
    val table = institutionEmailsTable2018
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
