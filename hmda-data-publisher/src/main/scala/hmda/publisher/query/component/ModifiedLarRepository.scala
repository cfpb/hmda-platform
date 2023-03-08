package hmda.publisher.query.component

import hmda.publisher.query.lar.ModifiedLarEntityImpl
import hmda.query.DbConfiguration.dbConfig.profile.api._
import hmda.query.repository.TableRepository
import slick.basic.{ DatabaseConfig, DatabasePublisher }
import slick.jdbc.{ JdbcProfile, ResultSetConcurrency, ResultSetType }

import scala.concurrent.Future
class ModifiedLarRepository(
  val config: DatabaseConfig[JdbcProfile],
  override val table: TableQuery[ModifiedLarTable]) extends TableRepository[ModifiedLarTable, String] {

  override def getId(row: ModifiedLarTable): config.profile.api.Rep[Id] = row.lei

  def createSchema() = db.run(table.schema.create)

  def dropSchema() = db.run(table.schema.drop)

  def insert(ts: ModifiedLarEntityImpl): Future[Int] =
    db.run(table += ts)

  def findByLei(lei: String): Future[Seq[ModifiedLarEntityImpl]] =
    db.run(table.filter(_.lei === lei).result)

  def deleteByLei(lei: String): Future[Int] =
    db.run(table.filter(_.lei === lei).delete)

  def count(): Future[Int] =
    db.run(table.size.result)

  def getAllLARs(bankIgnoreList: Array[String]): DatabasePublisher[ModifiedLarEntityImpl] =
    db.stream(
      table
        .filterNot(_.lei.toUpperCase inSet bankIgnoreList)
        .result
        .withStatementParameters(
          rsType = ResultSetType.ForwardOnly,
          rsConcurrency = ResultSetConcurrency.ReadOnly,
          fetchSize = 1000
        )
        .transactionally
    )
}