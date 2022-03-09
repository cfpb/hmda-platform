package hmda.publisher.query.component

import hmda.query.DbConfiguration.dbConfig.profile.api._
import hmda.query.repository.TableRepository
import hmda.query.ts.TransmittalSheetEntity

import scala.concurrent.Future

trait TsRepository[TsTable <: Table[TransmittalSheetEntity]] extends TableRepository[TsTable, String] {

  def count(): Future[Int]
  def getAllSheets(bankIgnoreList: Array[String]): Future[Seq[TransmittalSheetEntity]]
  def createSchema(): Unit
  def dropSchema(): Unit

  def insert(ts: TransmittalSheetEntity): Future[Int]

  def findByLei(lei: String): Future[Seq[TransmittalSheetEntity]]

  def deleteByLei(lei: String): Future[Int]
}