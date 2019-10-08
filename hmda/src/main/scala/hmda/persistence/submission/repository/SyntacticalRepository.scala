package hmda.persistence.submission.repositories

import com.outworkers.phantom.dsl._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class SyntacticalCheck(submissionId: String, hashedInfo: String)

abstract class SyntacticalRepository extends Table[SyntacticalRepository, SyntacticalCheck] with RootConnector {

  /**
  CREATE TABLE distinct_count_storage (submissionId text, hashedInfo text, PRIMARY KEY (submissionId, hashedInfo));
    */
  object submissionId extends StringColumn with PartitionKey
  object hashedInfo   extends StringColumn with ClusteringOrder

  override def tableName: String = "distinct_count_storage"

  def find(submissionId: String, hashedInfo: String): Future[Option[SyntacticalCheck]] =
    select
      .where(_.submissionId eqs submissionId)
      .and(_.hashedInfo eqs hashedInfo)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .one()

  def persist(submissionId: String, hashedInfo: String, timeout: FiniteDuration): Future[SyntacticalCheck] =
    insert
      .value(_.submissionId, submissionId)
      .value(_.hashedInfo, hashedInfo)
      .ttl(timeout)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .future()
      .map(_ => SyntacticalCheck(submissionId, hashedInfo))

  // returns true if the record was persisted and false if it was not
  def persistsIfNotExists(submissionId: String, hashedInfo: String, timeout: FiniteDuration): Future[Boolean] =
    insert
      .value(_.submissionId, submissionId)
      .value(_.hashedInfo, hashedInfo)
      .ttl(timeout)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .ifNotExists()
      .future()
      .map(rs => rs.wasApplied())

  def remove(submissionId: String): Future[Unit] =
    delete
      .where(_.submissionId eqs submissionId)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .future()
      .map(_ => ())

  def count(submissionId: String): Future[Long] =
    select
      .count()
      .where(_.submissionId eqs submissionId)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .one()
      .map(_.getOrElse(0))
}
