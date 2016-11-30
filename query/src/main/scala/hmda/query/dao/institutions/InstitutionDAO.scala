package hmda.query.dao.institutions

import hmda.query.model.institutions.InstitutionQuery
import slick.driver.JdbcProfile

trait InstitutionDAO { profile: JdbcProfile =>

  import profile.api._

  protected class Institutions(tag: Tag) extends Table[InstitutionQuery](tag, "institutions") {

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

  private val institutions = TableQuery[Institutions]

  def createSchema(): DBIO[Unit] = {
    institutions.schema.create
  }

  def insertOrUpdate(i: InstitutionQuery): DBIO[Int] = {
    institutions.insertOrUpdate(i)
  }

  def update(i: InstitutionQuery): DBIO[Int] = {
    institutions.update(i)
  }

  def get(id: String): DBIO[Option[InstitutionQuery]] = {
    (for (i <- institutions if i.id === id) yield i.value).result.headOption
  }
}

