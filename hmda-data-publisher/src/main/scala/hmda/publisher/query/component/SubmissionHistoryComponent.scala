package hmda.publisher.query.component

import hmda.publisher.helper.PGTableNameLoader
import hmda.publisher.query.submissionhistory.SubmissionHistoryEntity
import hmda.query.DbConfiguration.dbConfig
import hmda.query.repository.TableRepository
import hmda.query.ts.TransmittalSheetEntity
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future


trait SubmissionHistoryComponent extends PGTableNameLoader{

  import dbConfig.profile.api._

  class SubmissionHistoryTable(tag: Tag)
    extends Table[SubmissionHistoryEntity](tag, submissionHistoryTableName) with SubmissionHistoryComponent {
    def id              = column[Int]("id")
    def lei = column[String]("lei")
    def submissionId= column[String]("submission_id")
    def signDate = column[Option[Long]]("sign_date")

    def * =
      (lei, submissionId, signDate) <> (SubmissionHistoryEntity.tupled, SubmissionHistoryEntity.unapply)

  }
  val submissionHistoryTable = TableQuery[SubmissionHistoryTable]

class SubmissionHistoryRepository(val config: DatabaseConfig[JdbcProfile])
    extends TableRepository[SubmissionHistoryTable, Int] {
  val table = submissionHistoryTable
  def getId(table: SubmissionHistoryTable) = table.id
  def deleteById(id: Int) = db.run(filterById(id).delete)

  def createSchema() = db.run(table.schema.create)
  def dropSchema() = db.run(table.schema.drop)

  def findByLei(lei: String) = {
    db.run(table.filter(_.lei === lei).result)
  }


  def findFirstSignDate(lei: String,year: Int): Future[Seq[Long]] = {
    val submissionIdLikeStatment = s"${lei}-${year}-%"
    val excludeQuarterlyLikeStatment = s"%-Q%"
    db.run {
      sql"""
        SELECT MIN(sign_date) from #${submissionHistoryTableName}
        WHERE submission_id LIKE $submissionIdLikeStatment AND
        NOT LIKE $excludeQuarterlyLikeStatment
        """.as[Long]
    }
  }
}
}