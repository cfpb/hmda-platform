package hmda.submissionerrors.repositories

import java.sql.Timestamp
import java.time.{ ZoneOffset, ZonedDateTime }

import hmda.model.filing.submission.SubmissionId
import monix.eval.Task
import slick.basic.DatabaseConfig
// $COVERAGE-OFF$
final case class SubmissionErrorRecord(
                                        lei: String,
                                        period: String,
                                        sequenceNumber: Int,
                                        submissionStatus: Int,
                                        createdDate: Timestamp,
                                        updatedDate: Timestamp,
                                        editName: String,
                                        loanData: Vector[String]
                                      )

final case class AddSubmissionError(
                                     editName: String,
                                     loanData: Vector[String]
                                   )

object PostgresSubmissionErrorRepository {
  def config(dbHoconPath: String): DatabaseConfig[PostgresEnhancedProfile] =
    DatabaseConfig.forConfig[PostgresEnhancedProfile](dbHoconPath)

  def make(config: DatabaseConfig[PostgresEnhancedProfile], tableName: String): SubmissionErrorRepository =
    new PostgresSubmissionErrorRepository(config, tableName)
}

private[repositories] class PostgresSubmissionErrorRepository(config: DatabaseConfig[PostgresEnhancedProfile], tableName: String)
  extends SubmissionErrorRepository {
  import config.db
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
    def editName         = column[String]("edit_name")
    def loanData         = column[Vector[String]]("loan_data")
    def pk               = primaryKey("submission_error_pk", (lei, period, sequenceNumber, editName))

    override def * =
      (lei, period, sequenceNumber, submissionStatus, createdDate, updatedDate, editName, loanData) <> (SubmissionErrorRecord.tupled, SubmissionErrorRecord.unapply)
  }

  private val tableQuery = TableQuery[SubmissionErrorTable](tag => SubmissionErrorTable(tableName)(tag))

  private def submissionPresentDBIO(submissionId: SubmissionId): DBIO[Boolean] =
    tableQuery
      .filter(row =>
        row.lei === submissionId.lei
          && row.period === submissionId.period.toString
          && row.sequenceNumber === submissionId.sequenceNumber
      )
      .exists
      .result

  // Note: We use Task to delay execution to get more control on how the Slick database queries are executed
  // Note: Task is just a description of the program we would like to run, nothing is run yet until we call one
  // of the unsafeRun* methods
  def submissionPresent(submissionId: SubmissionId): Task[Boolean] =
  // It is very important to ensure that whenever a Future is about to be created, we immediately wrap it in
  // Task.deferFuture in order to delay immediate execution
    Task.deferFuture(db.run(submissionPresentDBIO(submissionId)))

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

    Task
      .deferFuture(db.run(submissionPresentDBIO(submissionId).flatMap {
        case true  => tableQuery ++= Nil
        case false => tableQuery ++= records
      }(db.ioExecutionContext)))
      .void
  }
}
// $COVERAGE-ON$