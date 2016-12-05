package hmda.query.dao.institutions

import hmda.query.dao.HmdaRepository
import hmda.query.model.institutions.InstitutionEntity
import slick.driver.JdbcProfile

trait InstitutionRepository extends HmdaRepository {
  profile: JdbcProfile =>

  import profile.api._

  class InstitutionTable(tag: Tag) extends HmdaTable[InstitutionEntity](tag, None, "institutions") {

    override val id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")
    def cra = column[Boolean]("cra")
    def agency = column[Int]("agency")
    def institutionType = column[String]("type")
    def hasParent = column[Boolean]("parent")
    def status = column[Int]("status")
    def filingPeriod = column[Int]("period")

    override def * = (id, name, cra, agency, institutionType, hasParent, status, filingPeriod) <> (InstitutionEntity.tupled, InstitutionEntity.unapply)

  }

  class InstitutionBaseRepository extends HmdaBaseRepository[InstitutionTable, InstitutionEntity](TableQuery[InstitutionTable]) {

  }

}
