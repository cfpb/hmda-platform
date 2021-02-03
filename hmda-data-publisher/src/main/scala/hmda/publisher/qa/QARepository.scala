package hmda.publisher.qa

import hmda.query.DbConfiguration.dbConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait QARepository[-T] {

  def tableName: String

  def deletePreviousRecords(timeStamp: Long): Future[Unit]

  def saveAll(batch: Seq[T], fileName: String, timeStamp: Long): Future[Unit]

}

object QARepository {

  import dbConfig.profile.api._

  class Default[Entity, QATable <: QATableBase[Entity]](val config: DatabaseConfig[JdbcProfile], val table: TableQuery[QATable])(
    implicit ec: ExecutionContext
  ) extends QARepository[Entity] {
    override def tableName: String = table.baseTableRow.tableName

    override def deletePreviousRecords(timeStamp: Long): Future[Unit] =
      config.db.run(table.filter(_.timeStamp =!= timeStamp).delete).map(_ => ())

    override def saveAll(batch: Seq[Entity], fileName: String,timeStamp:Long): Future[Unit] =
      config.db.run(table ++= batch.map(r => QAEntity(r, fileName,timeStamp))).map(_ => ())
  }

  object NoOp extends QARepository[Any] {
    override def tableName: String                                            = "<noop>"
    override def deletePreviousRecords(timeStamp: Long): Future[Unit] = Future.unit
    override def saveAll(batch: Seq[Any], fileName: String, timeStamp: Long): Future[Unit]     = Future.unit
  }

}