package hmda.persistence.submission.repositories

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class SyntacticalCheck(submissionId: String, hashedLar: String)

abstract class SyntacticalRepository
    extends Table[SyntacticalRepository, SyntacticalCheck]
    with RootConnector {

  /**
    * CREATE TABLE distinct_count_storage (
    *   submissionid string,
    *   hashedinfo string
    *   PRIMARY KEY((submissionid), hashedinfo)
    * )
    */
  object submissionId extends StringColumn with PartitionKey
  object hashedLar extends StringColumn with ClusteringOrder

  override def tableName: String = "distinct_count_storage"

  def find(submissionId: String,
           hashedLar: String): Future[Option[SyntacticalCheck]] =
    select
      .where(_.submissionId eqs submissionId)
      .and(_.hashedLar eqs hashedLar)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .one()

  def persist(submissionId: String,
              hashedLar: String,
              timeoutInSeconds: Long): Future[SyntacticalCheck] =
    insert
      .value(_.submissionId, submissionId)
      .value(_.hashedLar, hashedLar)
      .ttl(timeoutInSeconds)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .future()
      .map(_ => SyntacticalCheck(submissionId, hashedLar))

  // if the record was inserted then this evaluates to true otherwise false
  def persistIfNotExists(submissionId: String,
                         hashedLar: String,
                         timeoutInSeconds: Long): Future[Boolean] = {
    insert
      .value(_.submissionId, submissionId)
      .value(_.hashedLar, hashedLar)
      .ttl(timeoutInSeconds)
      .ifNotExists()
      .future()
      .map(rs => rs.value().exists(row => row.isNull(1))) // the second column appears if the row is already present
  }

  // this will be a distinct count since we upsert entries that are the same
  def count(submissionId: String): Future[Long] =
    select
      .count()
      .where(_.submissionId eqs submissionId)
      .consistencyLevel_=(ConsistencyLevel.QUORUM)
      .one()
      .map(_.getOrElse(0))
}
