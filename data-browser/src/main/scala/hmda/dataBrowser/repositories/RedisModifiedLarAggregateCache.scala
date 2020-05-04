package hmda.dataBrowser.repositories

import hmda.dataBrowser.models.{ FilerInstitutionResponse, QueryField, Statistic }
import io.lettuce.core.api.async.RedisAsyncCommands
import monix.eval.Task

import scala.compat.java8.FutureConverters._
import io.circe.parser._
import io.circe.syntax._
import cats.implicits._
import io.circe.{ Decoder, Encoder }

import scala.concurrent.duration.FiniteDuration

// $COVERAGE-OFF$
// Talks to Redis via Redis4Cats
class RedisModifiedLarAggregateCache(redisClient: Task[RedisAsyncCommands[String, String]], timeToLive: FiniteDuration) extends Cache {
  private val Prefix = "AGG"

  private def findAndParse[A: Decoder](key: String): Task[Option[A]] =
    redisClient.flatMap { redisClient =>
      Task
        .deferFuture(redisClient.get(key).toScala)
        .map(Option(_))
        .map(optResponse => optResponse.flatMap(stringResponse => decode[A](stringResponse).toOption))
    }.onErrorFallbackTo(Task.now(None))

  private def updateAndSetTTL[A: Encoder](key: String, value: A): Task[A] =
    (for {
      redisClient <- redisClient
      _           <- Task.deferFuture(redisClient.set(key, value.asJson.noSpaces).toScala)
      _           <- Task.deferFuture(redisClient.pexpire(key, timeToLive.toMillis).toScala)
    } yield value).onErrorFallbackTo(Task.now(value))

  private def invalidateKey(key: String): Task[Unit] =
    redisClient.flatMap { redisClient =>
      Task
        .deferFuture(redisClient.pexpire(key, timeToLive.toMillis).toScala)
        .map(_ => ())
    }.onErrorFallbackTo(Task.unit)

  private def key(queryFields: List[QueryField]): String = {
    // ensure we get a stable sorting order so we form keys correctly in Redis
    val sortedQueryFields = queryFields.sortBy(_.name)
    val redisKey = sortedQueryFields
      .map(field => s"${field.name}:${field.values.mkString("|")}")
      .mkString(":")
    s"$Prefix:$redisKey"
  }

  override def find(queryFields: List[QueryField]): Task[Option[Statistic]] = {
    val redisKey = key(queryFields)
    findAndParse[Statistic](redisKey)
  }

  override def findFilers(filerFields: List[QueryField]): Task[Option[FilerInstitutionResponse]] = {
    val redisKey = key(filerFields)
    findAndParse[FilerInstitutionResponse](redisKey)
  }

  override def update(queryFields: List[QueryField], statistic: Statistic): Task[Statistic] = {
    val redisKey = key(queryFields)
    updateAndSetTTL(redisKey, statistic)
  }

  override def updateFilers(
                             queryFields: List[QueryField],
                             filerInstitutionResponse: FilerInstitutionResponse
                           ): Task[FilerInstitutionResponse] = {
    val redisKey = key(queryFields)
    updateAndSetTTL(redisKey.toString, filerInstitutionResponse)
  }

  override def invalidate(queryFields: List[QueryField]): Task[Unit] = {
    val redisKey = key(queryFields)
    invalidateKey(redisKey)
  }

  def healthCheck: Task[Unit] =
    redisClient
      .flatMap(client => Task.deferFuture(client.ping().toScala))
      .void
}
// $COVERAGE-ON$