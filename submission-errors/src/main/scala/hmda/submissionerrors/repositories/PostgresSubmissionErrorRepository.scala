
package hmda.submissionerrors.repositories

import java.sql.Timestamp
import java.time.{ ZoneOffset, ZonedDateTime }

import hmda.model.filing.submission.SubmissionId
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

final case class SubmissionErrorRecord(
                                        lei: String,
                                        period: String,
                                        sequenceNumber: Int,
                                        submissionStatus: Int,
                                        createdDate: Timestamp,
                                        updatedDate: Timestamp,
                                        editName: String,
                                        loanData: String
                                      )

final case class AddSubmissionError(
                                     editName: String,
                                     loanData: String
                                   )

class PostgresSubmissionErrorRepository(config: DatabaseConfig[JdbcProfile], tableName: String) {
  import config.profile.api._

  object SubmissionErrorTable {
    def apply(tableName: String)(tag: Tag): SubmissionErrorTable = new SubmissionErrorTable(tableName)(tag)
  }
  private[PostgresSubmissionErrorRepository] class SubmissionErrorTable(tableName: String)(tag: Tag)
    extends Table[SubmissionErrorRecord](tag, tableName) {
    def lei              = column[String]("lei")
    def period           = column[String]("period")
    def sequenceNumber   = column[Int]("sequence_number")
    def submissionStatus = column[Int]("submission_status")
    def createdDate      = column[Timestamp]("created_date")
    def updatedDate      = column[Timestamp]("updated_date")
    def editName         = column[String]("edit_names")
    def loanData         = column[String]("loan_data")
    def pk               = primaryKey("submission_error_pk", (lei, period, sequenceNumber))

    override def * =
      (lei, period, sequenceNumber, submissionStatus, createdDate, updatedDate, editName, loanData)
        .mapTo[SubmissionErrorRecord]
  }

  private val tableQuery = TableQuery[SubmissionErrorTable](tag => SubmissionErrorTable(tableName)(tag))

  // Note: We use Task to delay execution to get more control on how the Slick database queries are executed
  // Note: Task is just a description of the program we would like to run, nothing is run yet until we call one
  // of the unsafeRun* methods
  // We use Task.shift because we don't want the rest of the computation to run on the database thread pool and
  // instead we shift it to the default thread pool to reduce contention on the database
  // one <* two just means run effect one then run effect two but discard the result of effect two and use the result of
  // effect one
  def submissionPresent(submissionId: SubmissionId): Task[Boolean] =
    Task.fromFuture(
      config.db.run(
        tableQuery
          .filter(row =>
            row.lei === submissionId.lei
              && row.period === submissionId.period.toString
              && row.sequenceNumber === submissionId.sequenceNumber
          )
          .exists
          .result
      )
    ) <* Task.shift

  def add(submissionId: SubmissionId, submissionStatus: Int, info: List[AddSubmissionError]): Task[Unit] = {
    import submissionId._
    val now = {
      val zdt    = ZonedDateTime.now()
      val utcZdt = zdt.withZoneSameInstant(ZoneOffset.UTC)
      Timestamp.valueOf(utcZdt.toLocalDateTime)
    }
    val records = info.map(a =>
      SubmissionErrorRecord(
        lei = lei,
        period = period.toString,
        sequenceNumber = sequenceNumber,
        submissionStatus = submissionStatus,
        createdDate = now,
        updatedDate = now,
        a.editName,
        a.loanData
      )
    )

    // Note: We use insertOrUpdate because we could potentially reprocess a file and we don't want inserts to fail
    // with a conflict error
    Task
      .fromFuture(config.db.run(DBIO.sequence(records.map(tableQuery.insertOrUpdate))))
      .void <* Task.shift
  }
}