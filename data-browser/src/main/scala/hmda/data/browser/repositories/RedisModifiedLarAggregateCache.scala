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
  private val ActionTaken = "ACTION"
  private val Race = "RACE"

  private def findAndParse(key: String): Task[Option[Statistic]] =
    Task
      .deferFuture(redisClient.get(key).toScala)
      .map(Option(_))
      .map(optResponse =>
        optResponse.flatMap(stringResponse =>
          decode[Statistic](stringResponse).toOption))

  override def find(msaMd: Int,
                    actionTaken: Int,
                    race: String): Task[Option[Statistic]] =
    findAndParse(s"$Prefix:$MsaMd:$msaMd:$ActionTaken:$actionTaken:$Race:$race")

  override def find(state: String,
                    actionTaken: Int,
                    race: String): Task[Option[Statistic]] =
    findAndParse(s"$Prefix:$State:$state:$ActionTaken:$actionTaken:$Race:$race")

  override def find(actionTaken: Int, race: String): Task[Option[Statistic]] =
    findAndParse(s"$Prefix:$Nationwide:$ActionTaken:$actionTaken:$Race:$race")

  override def find(msaMd: Int,
                    state: String,
                    actionTaken: Int,
                    race: String): Task[Option[Statistic]] = {
    val key =
      s"$Prefix:$MsaMd:$msaMd:$State:$state:$ActionTaken:$actionTaken:$Race:$race"
    findAndParse(key)
  }

  private def updateAndSetTTL(key: String, value: Statistic): Task[Statistic] =
    for {
      _ <- Task.deferFuture(redisClient.set(key, value.asJson.noSpaces).toScala)
      _ <- Task.deferFuture(
        redisClient.pexpire(key, timeToLive.toMillis).toScala)
    } yield value

  override def update(msaMd: Int,
                      actionTaken: Int,
                      race: String,
                      stat: Statistic): Task[Statistic] = {
    val key = s"$Prefix:$MsaMd:$msaMd:$ActionTaken:$actionTaken:$Race:$race"
    updateAndSetTTL(key, stat)
  }

  override def update(state: String,
                      actionTaken: Int,
                      race: String,
                      stat: Statistic): Task[Statistic] = {
    val key = s"$Prefix:$State:$state:$ActionTaken:$actionTaken:$Race:$race"
    updateAndSetTTL(key, stat)
  }

  override def update(actionTaken: Int,
                      race: String,
                      stat: Statistic): Task[Statistic] = {
    val key = s"$Prefix:$Nationwide:$ActionTaken:$actionTaken:$Race:$race"
    updateAndSetTTL(key, stat)
  }

  override def update(msaMd: Int,
                      state: String,
                      actionTaken: Int,
                      race: String,
                      stat: Statistic): Task[Statistic] = {
    val key =
      s"$Prefix:$MsaMd:$msaMd:$State:$state:$ActionTaken:$actionTaken:$Race:$race"
    updateAndSetTTL(key, stat)
  }

  private def invalidateKey(key: String): Task[Unit] =
    Task
      .deferFuture(redisClient.pexpire(key, timeToLive.toMillis).toScala)
      .map(_ => Task.unit)

  override def invalidate(msaMd: Int,
                          actionTaken: Int,
                          race: String): Task[Unit] =
    invalidateKey(
      s"$Prefix:$MsaMd:$msaMd:$ActionTaken:$actionTaken:$Race:$race")

  override def invalidate(state: String,
                          actionTaken: Int,
                          race: String): Task[Unit] =
    invalidateKey(
      s"$Prefix:$State:$state:$ActionTaken:$actionTaken:$Race:$race")

  override def invalidate(actionTaken: Int, race: String): Task[Unit] =
    invalidateKey(s"$Prefix:$Nationwide:$ActionTaken:$actionTaken:$Race:$race")

  override def invalidate(msaMd: Int,
                          state: String,
                          actionTaken: Int,
                          race: String): Task[Unit] =
    invalidateKey(
      s"$Prefix:$MsaMd:$msaMd:$State:$state:$ActionTaken:$actionTaken:$Race:$race")
}
