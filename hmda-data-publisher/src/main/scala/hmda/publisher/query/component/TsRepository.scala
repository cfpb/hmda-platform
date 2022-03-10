package hmda.publisher.query.component

import hmda.query.DbConfiguration.dbConfig.profile.api._
import hmda.query.repository.TableRepository
import hmda.query.ts.TransmittalSheetEntity
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class TsRepository[TsTable <: TransmittalSheetTable](
  val config: DatabaseConfig[JdbcProfile],
  val table: TableQuery[TsTable]
) extends TableRepository[TsTable, String] {

  def getId(row: TsTable): config.profile.api.Rep[Id] =
    row.lei

  def createSchema(): Future[Unit] =
    db.run(table.schema.create)

  def dropSchema(): Future[Unit] =
    db.run(table.schema.drop)

  def insert(ts: TransmittalSheetEntity): Future[Int] =
    db.run(table += ts)

  def findByLei(lei: String): Future[Seq[TransmittalSheetEntity]] =
    db.run(table.filter(_.lei === lei).result)

  def deleteByLei(lei: String): Future[Int] =
    db.run(table.filter(_.lei === lei).delete)

  def count(): Future[Int] =
    db.run(table.size.result)

  def getAllSheets(bankIgnoreList: Array[String]): Future[Seq[TransmittalSheetEntity]] =
    db.run(table.filterNot(_.lei.toUpperCase inSet bankIgnoreList).result)
}