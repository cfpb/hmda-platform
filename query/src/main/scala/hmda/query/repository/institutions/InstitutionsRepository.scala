package hmda.query.repository.institutions

import hmda.query.Db
import hmda.query.model.institutions.InstitutionQuery
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class InstitutionsRepository(val config: DatabaseConfig[JdbcProfile]) extends Db with InstitutionsTable {

  import config.profile.api._

  def createSchema(): Future[Unit] = {
    db.run(institutions.schema.create)
  }

  def dropSchema(): Future[Unit] = {
    db.run(institutions.schema.drop)
  }

  def insertOrUpdate(i: InstitutionQuery): Future[Int] = {
    db.run(institutions.insertOrUpdate(i))
  }

  def update(i: InstitutionQuery): Future[Int] = {
    db.run(institutions.update(i))
  }

  def get(id: String): Future[Option[InstitutionQuery]] = {
    db.run((for (i <- institutions if i.id === id) yield i.value).result.headOption)
  }

}
