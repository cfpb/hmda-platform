package hmda.dataBrowser.repositories

import hmda.dataBrowser.models.{ FilerInstitutionResponse2017, FilerInstitutionResponseLatest, QueryField, LarQueryField, Statistic }
import hmda.dataBrowser.Settings
import io.lettuce.core.api.async.RedisAsyncCommands
import monix.eval.Task

import scala.compat.java8.FutureConverters._
import io.circe.parser._
import io.circe.syntax._
import cats.implicits._
import io.circe.{ Decoder, Encoder }
import org.slf4j.Logger

import scala.concurrent.duration.FiniteDuration

// $COVERAGE-OFF$
// Talks to Redis via Redis4Cats
class RedisModifiedLarAggregateCache(redisClient: Task[RedisAsyncCommands[String, String]], logger: Logger, timeToLive: FiniteDuration) extends Cache with Settings {
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

  private def key(instQueryField: QueryField, geoQueryField: QueryField, hmdaQueries: List[LarQueryField], year: Int): String = {
    // ensure we get a stable sorting order so we form keys correctly in Redis
    val instGeoQueryField = List(instQueryField, geoQueryField)
    val hmdaQueryField = hmdaQueries.sortBy(_.name)
    val instGeoRedisKey = instGeoQueryField
      .map(field => s"${field.name}:${field.values.mkString("|")}")
      .mkString(":")
    val hmdaRedisKey = hmdaQueryField
      .map(field => s"${field.name}:${field.value.mkString("|")}")
      .mkString(":")

    s"$Prefix:year:" + year.toString + ":table:" + database.tableNameSelector(year) + s":$instGeoRedisKey:$hmdaRedisKey"
  }

  private def filerKey(queryFields: List[QueryField], year: Int): String = {
    // ensure we get a stable sorting order so we form keys correctly in Redis
    val sortedQueryFields = queryFields.sortBy(_.name)
    val redisKey = sortedQueryFields
      .map(field => s"${field.name}:${field.values.mkString("|")}")
      .mkString(":")

    s"$Prefix:year:" + year.toString + ":table:" + database.tableNameSelector(year) + s":$redisKey"
  }

  override def find(instQueryField: QueryField, geoQueryField: QueryField, hmdaQueries: List[LarQueryField], year: Int): Task[Option[Statistic]] = {
    val redisKey = key(instQueryField, geoQueryField, hmdaQueries, year)
    logger.info("Redis Key: " + redisKey)
    println("CACHE:" +  hmdaQueries)
    findAndParse[Statistic](redisKey)
  }

  override def findFilers2017(filerFields: List[QueryField], year: Int): Task[Option[FilerInstitutionResponse2017]] = {
    val redisKey = filerKey(filerFields, year)
    println(redisKey)
    findAndParse[FilerInstitutionResponse2017](redisKey)
  }

  override def findFilers2018(filerFields: List[QueryField], year: Int): Task[Option[FilerInstitutionResponseLatest]] = {
    val redisKey = filerKey(filerFields, year)
    println(redisKey)
    findAndParse[FilerInstitutionResponseLatest](redisKey)
  }

  override def update(instQueryField: QueryField, geoQueryField: QueryField, hmdaQueries: List[LarQueryField], year: Int, statistic: Statistic): Task[Statistic] = {
    val redisKey = key(instQueryField, geoQueryField, hmdaQueries, year)
    updateAndSetTTL(redisKey, statistic)
  }

  override def updateFilers2017(
    queryFields: List[QueryField],
    year: Int,
    filerInstitutionResponse: FilerInstitutionResponse2017
  ): Task[FilerInstitutionResponse2017] = {
    val redisKey = filerKey(queryFields, year)
    updateAndSetTTL(redisKey, filerInstitutionResponse)
  }

  override def updateFilers2018(
    queryFields: List[QueryField],
    year: Int,
    filerInstitutionResponse: FilerInstitutionResponseLatest
  ): Task[FilerInstitutionResponseLatest] = {
    val redisKey = filerKey(queryFields, year)
    updateAndSetTTL(redisKey, filerInstitutionResponse)
  }

  override def invalidate(instQueryField: QueryField, geoQueryField: QueryField, hmdaQueries: List[LarQueryField], year: Int): Task[Unit] = {
    val redisKey = key(instQueryField, geoQueryField, hmdaQueries, year)
    invalidateKey(redisKey)
  }

  def healthCheck: Task[Unit] =
    redisClient
      .flatMap(client => Task.deferFuture(client.ping().toScala))
      .void
}
// $COVERAGE-ON$