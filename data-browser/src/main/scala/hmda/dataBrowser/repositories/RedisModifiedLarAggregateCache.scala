package hmda.dataBrowser.repositories

import hmda.dataBrowser.models.{ FilerInstitutionResponse2017, FilerInstitutionResponse2018, QueryField, QueryFields, Statistic }
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

  private def key(queryFields: List[QueryField], year: Int): String = {
    // The year was originally a query field but it was split up later, we do this to preserve backwards compatibility
    val yearQuery = QueryField(name = "year", year.toString :: Nil, dbName = "filing_year")
    // ensure we get a stable sorting order so we form keys correctly in Redis
    val sortedQueryFields = (yearQuery :: queryFields).sortBy(_.name)
    val redisKey = sortedQueryFields
      .map(field => s"${field.name}:${field.values.mkString("|")}")
      .mkString(":")

    s"$Prefix:$redisKey"
  }

  override def find(queryFields: List[QueryField], year: Int): Task[Option[Statistic]] = {
    val redisKey = key(queryFields, year)
    findAndParse[Statistic](redisKey)
  }

  override def findFilers2017(filerFields: List[QueryField], year: Int): Task[Option[FilerInstitutionResponse2017]] = {
    val redisKey = key(filerFields, year)
    println(redisKey)
    findAndParse[FilerInstitutionResponse2017](redisKey)
  }

  override def findFilers2018(filerFields: List[QueryField], year: Int): Task[Option[FilerInstitutionResponse2018]] = {
    val redisKey = key(filerFields, year)
    println(redisKey)
    findAndParse[FilerInstitutionResponse2018](redisKey)
  }

  override def update(queryFields: List[QueryField], year: Int, statistic: Statistic): Task[Statistic] = {
    val redisKey = key(queryFields, year)
    updateAndSetTTL(redisKey, statistic)
  }

  override def updateFilers2017(
                                 queryFields: List[QueryField],
                                 year: Int,
                                 filerInstitutionResponse: FilerInstitutionResponse2017
                               ): Task[FilerInstitutionResponse2017] = {
    val redisKey = key(queryFields, year)
    updateAndSetTTL(redisKey, filerInstitutionResponse)
  }

  override def updateFilers2018(
                                 queryFields: List[QueryField],
                                 year: Int,
                                 filerInstitutionResponse: FilerInstitutionResponse2018
                               ): Task[FilerInstitutionResponse2018] = {
    val redisKey = key(queryFields, year)
    updateAndSetTTL(redisKey, filerInstitutionResponse)
  }

  override def invalidate(queryFields: List[QueryField], year: Int): Task[Unit] = {
    val redisKey = key(queryFields, year)
    invalidateKey(redisKey)
  }

  def healthCheck: Task[Unit] =
    redisClient
      .flatMap(client => Task.deferFuture(client.ping().toScala))
      .void
}
// $COVERAGE-ON$