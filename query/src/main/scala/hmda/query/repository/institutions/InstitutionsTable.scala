package hmda.query.repository.institutions

import hmda.query.Db
import hmda.query.model.institutions.InstitutionQuery

trait InstitutionsTable { this: Db =>

  import config.profile.api._

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

  protected val institutions = TableQuery[Institutions]

}
