package hmda.query.ts

import hmda.query.DbConfiguration.dbConfig.profile.api._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class TransmittalSheetRepository(val config: DatabaseConfig[JdbcProfile], val tableName: String) extends TableRepository[TransmittalSheetTable, String] {
  override def table: config.profile.api.TableQuery[TransmittalSheetTable] = TableQuery[TransmittalSheetTable]((tag: Tag) => new TransmittalSheetTable(tag, tableName))

  override def getId(row: TransmittalSheetTable): config.profile.api.Rep[Id] = row.lei

  def createSchema(): Future[Unit] = db.run(table.schema.create)
  def dropSchema(): Future[Unit] = db.run(table.schema.drop)
}
