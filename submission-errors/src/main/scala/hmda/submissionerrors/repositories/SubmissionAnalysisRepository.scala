package hmda.submissionerrors.repositories

import hmda.model.filing.submission.{ Submission, SubmissionId }
import monix.eval.Task
import slick.basic.DatabaseConfig

// $COVERAGE-OFF$
trait SubmissionAnalysisRepository[T] {
  /**
   * Determines if a submission is already present in the repository
   * @param submissionId is the submission ID that consists of LEI + Period + Sequence Number
   * @return a Boolean (true if it exists/false if it does not)
   */
  def submissionPresent(submissionId: SubmissionId): Task[Boolean]

  /**
   * Inserts error-data pertaining to a submission. This also handles the case if a submission already exists and
   * in that case, a no-op will be done
   *
   * @param submissionId is the Submission ID that consists of LEI + Period + Sequence Number
   * @param submissionStatus is the submission status code
   * @param records is a list of error information to be added for a given Submission ID
   * @return
   */
  def add(submission: Submission, records: Iterable[T]): Task[Unit]
}

//abstract class PostgresSubmissionAnalysisRepository[T](config: DatabaseConfig[PostgresEnhancedProfile], tableName: String) extends SubmissionAnalysisRepository[T] {
//  import config.db
//  import config.profile.api._
//
//  protected val tq: TableQuery[Table[T]]
//
//  protected def submissionPresentDBIO(submissionId: SubmissionId): DBIO[Boolean]
//
//  /**
//   * Determines if a submission is already present in the repository
//   * @param submissionId is the submission ID that consists of LEI + Period + Sequence Number
//   * @return a Boolean (true if it exists/false if it does not)
//   */
//  def submissionPresent(submissionId: SubmissionId): Task[Boolean] =
//    Task.deferFuture(db.run(submissionPresentDBIO(submissionId)))
//
//  /**
//   * Inserts error-data pertaining to a submission. This also handles the case if a submission already exists and
//   * in that case, a no-op will be done
//   *
//   * @param submissionId is the Submission ID that consists of LEI + Period + Sequence Number
//   * @param submissionStatus is the submission status code
//   * @param records is a list of records to be added for a given Submission ID
//   * @return
//   */
//  def add(submission: Submission, records: Iterable[T]): Task[Unit] =
//    Task
//      .deferFuture(db.run(submissionPresentDBIO(submission.id).flatMap {
//        case true  => tq ++= Nil
//        case false => tq ++= records
//      }(db.ioExecutionContext)))
//      .void
//
//  override def createTable(): Task[Unit] = Task.deferFuture(db.run(tq.schema.createIfNotExists))
//}
// $COVERAGE-ON$