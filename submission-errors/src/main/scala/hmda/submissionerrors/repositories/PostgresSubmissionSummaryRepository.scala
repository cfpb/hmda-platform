package hmda.submissionerrors.repositories

import hmda.model.filing.submission.{ Submission, SubmissionId }
import monix.eval.Task
import slick.basic.DatabaseConfig
// $COVERAGE-OFF$
final case class SubmissionSummaryRecord(
                                        lei: String,
                                        period: String,
                                        sequenceNumber: Int,
                                        submissionStatus: Int,
                                        larCount: Int,
                                      )

object PostgresSubmissionSummaryRepository {
  def config(dbHoconPath: String): DatabaseConfig[PostgresEnhancedProfile] =
    DatabaseConfig.forConfig[PostgresEnhancedProfile](dbHoconPath)

  def make(config: DatabaseConfig[PostgresEnhancedProfile], tableName: String): SubmissionAnalysisRepository[SubmissionSummaryRecord] =
    new PostgresSubmissionSummaryRepository(config, tableName)
}
private[repositories] class PostgresSubmissionSummaryRepository(config: DatabaseConfig[PostgresEnhancedProfile], tableName: String)
//  extends PostgresSubmissionAnalysisRepository[SubmissionSummaryRecord](config, tableName)
  extends SubmissionAnalysisRepository[SubmissionSummaryRecord] {
  import config.db
  import config.profile.api._

  object SubmissionSummaryTable {
    def apply(tableName: String)(tag: Tag): SubmissionSummaryTable = new SubmissionSummaryTable(tableName)(tag)
  }
  private[PostgresSubmissionSummaryRepository] class SubmissionSummaryTable(tableName: String)(tag: Tag)
    extends Table[SubmissionSummaryRecord](tag, tableName) {
    def lei              = column[String]("lei")
    def period           = column[String]("period")
    def sequenceNumber   = column[Int]("sequence_number")
    def submissionStatus = column[Int]("submission_status")
    def larCount = column[Int]("lar_count")
    def pk               = primaryKey("submission_summary_pk", (lei, period, sequenceNumber))

    override def * =
      (lei, period, sequenceNumber, submissionStatus, larCount) <> (SubmissionSummaryRecord.tupled, SubmissionSummaryRecord.unapply)
  }

  private val tableQuery = TableQuery[SubmissionSummaryTable](tag => SubmissionSummaryTable(tableName)(tag))

//  protected val tq = tableQuery

  private def submissionPresentDBIO(submissionId: SubmissionId): DBIO[Boolean] = {
    println(f"Check sub summary exist: $submissionId")
    tableQuery
      .filter(row =>
        row.lei === submissionId.lei
          && row.period === submissionId.period.toString
          && row.sequenceNumber === submissionId.sequenceNumber
      )
      .exists
      .result
  }

  /**
   * Determines if a submission is already present in the repository
   *
   * @param submissionId is the submission ID that consists of LEI + Period + Sequence Number
   * @return a Boolean (true if it exists/false if it does not)
   */
  override def submissionPresent(submissionId: SubmissionId): Task[Boolean] =
    Task.deferFuture(db.run(submissionPresentDBIO(submissionId)))

  /**
   * Inserts error-data pertaining to a submission. This also handles the case if a submission already exists and
   * in that case, a no-op will be done
   *
   * @param submissionId     is the Submission ID that consists of LEI + Period + Sequence Number
   * @param submissionStatus is the submission status code
   * @param records          is a list of error information to be added for a given Submission ID
   * @return
   */
  override def add(submission: Submission, records: Iterable[SubmissionSummaryRecord]): Task[Unit] =
    Task
      .deferFuture(db.run(submissionPresentDBIO(submission.id).flatMap {
        case true  => tableQuery ++= Nil
        case false => tableQuery ++= records
      }(db.ioExecutionContext)))
      .void
}
// $COVERAGE-ON$