package hmda.dataBrowser.repositories

import hmda.dataBrowser.models.QueryField
import io.lettuce.core.api.async.RedisAsyncCommands
import monix.eval.Task

import scala.compat.java8.FutureConverters._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.duration.FiniteDuration

class RedisModifiedLarAggregateCache(
    redisClient: Task[RedisAsyncCommands[String, String]],
    timeToLive: FiniteDuration)
    extends ModifiedLarAggregateCache {
  private val Prefix = "AGG"

  private def findAndParse(key: String): Task[Option[Statistic]] = {
    redisClient
      .flatMap { redisClient =>
        Task
          .deferFuture(redisClient.get(key).toScala)
          .map(Option(_))
          .map(optResponse =>
            optResponse.flatMap(stringResponse =>
              decode[Statistic](stringResponse).toOption))
      }
      .onErrorFallbackTo(Task.now(None))
  }

  private def updateAndSetTTL(key: String, value: Statistic): Task[Statistic] =
    (for {
      redisClient <- redisClient
      _ <- Task.deferFuture(redisClient.set(key, value.asJson.noSpaces).toScala)
      _ <- Task.deferFuture(
        redisClient.pexpire(key, timeToLive.toMillis).toScala)
    } yield value).onErrorFallbackTo(Task.now(value))

  private def invalidateKey(key: String): Task[Unit] =
    redisClient
      .flatMap { redisClient =>
        Task
          .deferFuture(redisClient.pexpire(key, timeToLive.toMillis).toScala)
          .map(_ => ())
      }
      .onErrorFallbackTo(Task.unit)

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
    findAndParse(redisKey)
  }

  override def update(queryFields: List[QueryField],
                      statistic: Statistic): Task[Statistic] = {
    val redisKey = key(queryFields)
    updateAndSetTTL(redisKey, statistic)
  }

  override def invalidate(queryFields: List[QueryField]): Task[Unit] = {
    val redisKey = key(queryFields)
    invalidateKey(redisKey)
  }
}
