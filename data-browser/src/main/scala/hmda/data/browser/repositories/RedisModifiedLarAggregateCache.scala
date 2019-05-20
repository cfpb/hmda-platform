package hmda.data.browser.repositories

import io.lettuce.core.api.async.RedisAsyncCommands
import monix.eval.Task

import scala.compat.java8.FutureConverters._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.duration.FiniteDuration

class RedisModifiedLarAggregateCache(
    redisClient: RedisAsyncCommands[String, String],
    timeToLive: FiniteDuration)
    extends ModifiedLarAggregateCache {
  private val Prefix = "MLAR"
  private val Nationwide = "Nationwide"
  private val MsaMd = "MSAMD"
  private val State = "STATE"

  private def findAndParse(key: String): Task[Option[Statistic]] =
    Task
      .deferFuture(redisClient.get(key).toScala)
      .map(Option(_))
      .map(optResponse =>
        optResponse.flatMap(stringResponse =>
          decode[Statistic](stringResponse).toOption))

  override def find(msaMd: Int,
                    field1Name: String,
                    field1: String,
                    field2Name: String,
                    field2: String): Task[Option[Statistic]] =
    findAndParse(
      s"$Prefix:$MsaMd:$msaMd:$field1Name:$field1:$field2Name:$field2")

  override def find(state: String,
                    field1Name: String,
                    field1: String,
                    field2Name: String,
                    field2: String): Task[Option[Statistic]] =
    findAndParse(
      s"$Prefix:$State:$state:$field1Name:$field1:$field2Name:$field2")

  override def find(field1Name: String,
                    field1: String,
                    field2Name: String,
                    field2: String): Task[Option[Statistic]] =
    findAndParse(s"$Prefix:$Nationwide:$field1Name:$field1:$field2Name:$field2")

  override def find(msaMd: Int,
                    state: String,
                    field1Name: String,
                    field1: String,
                    field2Name: String,
                    field2: String): Task[Option[Statistic]] = {
    val key =
      s"$Prefix:$MsaMd:$msaMd:$State:$state:$field1Name:$field1:$field2Name:$field2"
    findAndParse(key)
  }

  private def updateAndSetTTL(key: String, value: Statistic): Task[Statistic] =
    for {
      _ <- Task.deferFuture(redisClient.set(key, value.asJson.noSpaces).toScala)
      _ <- Task.deferFuture(
        redisClient.pexpire(key, timeToLive.toMillis).toScala)
    } yield value

  override def update(msaMd: Int,
                      field1Name: String,
                      field1: String,
                      field2Name: String,
                      field2: String,
                      stat: Statistic): Task[Statistic] = {
    val key = s"$Prefix:$MsaMd:$msaMd:$field1Name:$field1:$field2Name:$field2"
    updateAndSetTTL(key, stat)
  }

  override def update(state: String,
                      field1Name: String,
                      field1: String,
                      field2Name: String,
                      field2: String,
                      stat: Statistic): Task[Statistic] = {
    val key = s"$Prefix:$State:$state:$field1Name:$field1:$field2Name:$field2"
    updateAndSetTTL(key, stat)
  }

  override def update(field1Name: String,
                      field1: String,
                      field2Name: String,
                      field2: String,
                      stat: Statistic): Task[Statistic] = {
    val key = s"$Prefix:$Nationwide:$field1Name:$field1:$field2Name:$field2"
    updateAndSetTTL(key, stat)
  }

  override def update(msaMd: Int,
                      state: String,
                      field1Name: String,
                      field1: String,
                      field2Name: String,
                      field2: String,
                      stat: Statistic): Task[Statistic] = {
    val key =
      s"$Prefix:$MsaMd:$msaMd:$State:$state:$field1Name:$field1:$field2Name:$field2"
    updateAndSetTTL(key, stat)
  }

  private def invalidateKey(key: String): Task[Unit] =
    Task
      .deferFuture(redisClient.pexpire(key, timeToLive.toMillis).toScala)
      .map(_ => Task.unit)

  override def invalidate(msaMd: Int,
                          field1Name: String,
                          field1: String,
                          field2Name: String,
                          field2: String): Task[Unit] =
    invalidateKey(
      s"$Prefix:$MsaMd:$msaMd:$field1Name:$field1:$field2Name:$field2")

  override def invalidate(state: String,
                          field1Name: String,
                          field1: String,
                          field2Name: String,
                          field2: String): Task[Unit] =
    invalidateKey(
      s"$Prefix:$State:$state:$field1Name:$field1:$field2Name:$field2")

  override def invalidate(field1Name: String,
                          field1: String,
                          field2Name: String,
                          field2: String): Task[Unit] =
    invalidateKey(
      s"$Prefix:$Nationwide:$field1Name:$field1:$field2Name:$field2")

  override def invalidate(msaMd: Int,
                          state: String,
                          field1Name: String,
                          field1: String,
                          field2Name: String,
                          field2: String): Task[Unit] =
    invalidateKey(
      s"$Prefix:$MsaMd:$msaMd:$State:$state:$field1Name:$field1:$field2Name:$field2")
}
