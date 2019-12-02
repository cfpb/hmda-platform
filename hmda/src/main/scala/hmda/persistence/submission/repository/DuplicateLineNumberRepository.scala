package hmda.persistence.submission.repository

import com.outworkers.phantom.dsl._
import cats.implicits._

import scala.concurrent.Future
import scala.concurrent.duration._

case class DuplicateLineNumberResult(submissionId: String, checkType: String, totalCount: Int, lineNumbers: Set[Int])

abstract class DuplicateLineNumberRepository extends Table[DuplicateLineNumberRepository, DuplicateLineNumberResult] {
  /**
  CREATE TABLE duplicate_line_number_storage (
        submissionId  text,
        checkType     text,
        totalCount    int,
        lineNumbers   set<int>
        PRIMARY KEY (submissionId, checkType)
      );
      NOTE: Ensure columns line up with case class fields as they use automatic derivation
   */
  object submissionId extends StringColumn with PartitionKey
  object checkType extends StringColumn with PartitionKey
  object totalCount extends IntColumn
  object lineNumbers extends SetColumn[Int]

  override def tableName: String = "duplicate_line_number_storage"

  def find(submissionId: String, checkType: String): Future[Option[DuplicateLineNumberResult]] =
    select
      .where(_.submissionId eqs submissionId)
      .and(_.checkType eqs checkType)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .one()

  def persist(submissionId: String, checkType: String, totalCount: Int, lineNumbers: Set[Int], timeout: FiniteDuration = 260.minutes): Future[DuplicateLineNumberResult] =
    insert
      .value(_.submissionId, submissionId)
      .value(_.checkType, checkType)
      .value(_.totalCount, totalCount)
      .value(_.lineNumbers, lineNumbers)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .ttl(timeout)
      .future()
      .as(DuplicateLineNumberResult(submissionId, checkType, totalCount, lineNumbers))

  def remove(submissionId: String, checkType: String): Future[Unit] =
    delete
      .where(_.submissionId eqs submissionId)
      .and(_.checkType eqs checkType)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .future()
      .void
}