package hmda.query.repository.institutions

import hmda.query.{ Db, DbConfiguration }
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.repository.Repository
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait InstitutionComponent { this: DbConfiguration =>
  import config.driver.api._

  class InstitutionsTable(tag: Tag) extends Table[InstitutionQuery](tag, "institutions") {
    def id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")
    def cra = column[Boolean]("cra")
    def agency = column[Int]("agency")
    def institutionType = column[String]("type")
    def hasParent = column[Boolean]("parent")
    def status = column[Int]("status")
    def filingPeriod = column[Int]("period")

    override def * = (id, name, cra, agency, institutionType, hasParent, status, filingPeriod) <> (InstitutionQuery.tupled, InstitutionQuery.unapply)
  }

  class InstitutionRepository(val config: DatabaseConfig[JdbcProfile]) extends Repository[InstitutionsTable, String] {
    val table = TableQuery[InstitutionsTable]
    def getId(table: InstitutionsTable) = table.id

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
    def deleteById(id: String) = db.run(filterById(id).delete)
  }

}
