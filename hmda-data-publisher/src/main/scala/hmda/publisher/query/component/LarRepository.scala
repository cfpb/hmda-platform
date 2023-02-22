package hmda.publisher.query.component

import hmda.publisher.query.lar.LarEntityImpl
import slick.basic.{ DatabaseConfig, DatabasePublisher }
import slick.jdbc.{ JdbcProfile, ResultSetConcurrency, ResultSetType }
import hmda.query.DbConfiguration.dbConfig.profile.api._
import hmda.query.repository.TableRepository

import scala.concurrent.Future

class LarRepository[LTable <: LarTable](val config: DatabaseConfig[JdbcProfile], val table: TableQuery[LTable])
  extends TableRepository[LTable, String] {

  override def getId(row: LTable): config.profile.api.Rep[Id] =
    row.lei

  // TODO: Unless is_quarterly is part of the projection, this excludes the column causing breakage so don't use
  // $COVERAGE-OFF$
  def createSchema() = db.run(table.schema.create)

  def dropSchema() = db.run(table.schema.drop)
  // $COVERAGE-ON$

  def insert(ts: LarEntityImpl): Future[Int] =
    db.run(table += ts)

  def findByLei(lei: String): Future[Seq[LarEntityImpl]] =
    db.run(table.filter(_.lei === lei).result)

  def deleteByLei(lei: String): Future[Int] =
    db.run(table.filter(_.lei === lei).delete)

  def count(): Future[Int] =
    db.run(table.size.result)

  def getAllLARsCount(bankIgnoreList: Array[String]): Future[Int] =
    db.run(getAllLARsQuery(bankIgnoreList).size.result)

  def getAllLARs(bankIgnoreList: Array[String]): DatabasePublisher[LarEntityImpl] =
    db.stream(
      getAllLARsQuery(bankIgnoreList).result
        .withStatementParameters(
          rsType = ResultSetType.ForwardOnly,
          rsConcurrency = ResultSetConcurrency.ReadOnly,
          fetchSize = 1000
        )
        .transactionally
    )

  protected def getAllLARsQuery(bankIgnoreList: Array[String]): Query[LarTable, LarEntityImpl, Seq] =
    table.filterNot(_.lei.toUpperCase inSet bankIgnoreList)
}
