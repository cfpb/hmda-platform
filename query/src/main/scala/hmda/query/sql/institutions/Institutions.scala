package hmda.query.sql.institutions

import hmda.query.model.institutions.InstitutionQuery
import slick.driver.H2Driver.api._

class Institutions(tag: Tag) extends Table[InstitutionQuery](tag, "institutions") {

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
