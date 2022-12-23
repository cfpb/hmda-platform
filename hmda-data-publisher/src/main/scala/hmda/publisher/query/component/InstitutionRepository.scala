package hmda.publisher.query.component

import hmda.publisher.query.panel.InstitutionEntity
import hmda.query.DbConfiguration.dbConfig.profile.api._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
class InstitutionRepository(
  val config: DatabaseConfig[JdbcProfile],
  override val table: TableQuery[InstitutionsTable])
  extends TableRepository[InstitutionsTable, String] {

  override def getId(row: InstitutionsTable): config.profile.api.Rep[Id] =
    row.lei

  def createSchema() = db.run(table.schema.create)

  def dropSchema() = db.run(table.schema.drop)

  def insert(institution: InstitutionEntity): Future[Int] =
    db.run(table += institution)

  def findByLei(lei: String): Future[Seq[InstitutionEntity]] =
    db.run(table.filter(_.lei === lei).result)

  //(x => (x.isX && x.name == "xyz"))
  def findActiveFilers(bankIgnoreList: Array[String]): Future[Seq[InstitutionEntity]] =
    db.run(
      table
        .filter(_.hmdaFiler === true)
        .filterNot(_.lei.toUpperCase inSet bankIgnoreList)
        .result
    )

  def getAllInstitutions(): Future[Seq[InstitutionEntity]] =
    db.run(table.result)

  def deleteByLei(lei: String): Future[Int] =
    db.run(table.filter(_.lei === lei).delete)

  def count(): Future[Int] =
    db.run(table.size.result)
}