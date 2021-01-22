package hmda.publisher.qa

import hmda.query.DbConfiguration.dbConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait QARepository[-T] {

  def tableName: String

  def deletePreviousRecords(currentFileName: String): Future[Unit]

  def saveAll(batch: Seq[T], fileName: String): Future[Unit]

}

object QARepository {

  import dbConfig.profile.api._

  class Default[Entity, QATable <: QATableBase[Entity]](val config: DatabaseConfig[JdbcProfile], val table: TableQuery[QATable])(
    implicit ec: ExecutionContext
  ) extends QARepository[Entity] {
    override def tableName: String = table.baseTableRow.tableName

    override def deletePreviousRecords(currentFileName: String): Future[Unit] =
      config.db.run(table.filter(_.fileName =!= currentFileName).delete).map(_ => ())

    override def saveAll(batch: Seq[Entity], fileName: String): Future[Unit] =
      config.db.run(table ++= batch.map(r => QAEntity(r, fileName))).map(_ => ())
  }

  object NoOp extends QARepository[Any] {
    override def tableName: String                                            = "<noop>"
    override def deletePreviousRecords(currentFileName: String): Future[Unit] = Future.unit
    override def saveAll(batch: Seq[Any], fileName: String): Future[Unit]     = Future.unit
  }

}