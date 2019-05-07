package hmda.data.browser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._
import hmda.data.browser.repositories.{
  ModifiedLarAggregateCache,
  ModifiedLarRepository,
  Statistic
}
import monix.eval.Task

class ModifiedLarBrowserService(repo: ModifiedLarRepository,
                                cache: ModifiedLarAggregateCache)
  extends BrowserService {

  /**
    * This is responsible for performing the following logic for all provided combinations of races and actions taken:
    *   1. find if the entry is in the cache
    *   2a. if the entry is in the cache, proceed to 3
    *   2b. if the entry is not in the cache then obtain it from the database and update the cache
    *   3. tag entry with race and action taken information
    *   4. serve all entries
    *
    * This function relies on callers of the function to use currying to cover all use cases:
    * - queries by MSAMD
    * - queries by State
    * - nationwide queries
    *
    * @param races refers to the races enumeration
    * @param actionsTaken refers to the actions taken enumeration
    * @param findInDatabase is a function that retrieves data from the database
    * @param findInCache is a function that retrieves data from the cache (the data may not be in the cache)
    * @param updateCache is a function that updates the cache with the data from the database
    * @return
    */
  private def fetchAgg(
                        races: Seq[Race],
                        actionsTaken: Seq[ActionTaken],
                        findInDatabase: (Race, ActionTaken) => Task[Statistic],
                        findInCache: (Race, ActionTaken) => Task[Option[Statistic]],
                        updateCache: (Race, ActionTaken, Statistic) => Task[Statistic])
  : Task[Seq[Aggregation]] = {
    val taskList = for {
      race <- races
      actionTaken <- actionsTaken
    } yield {
      // this is only executed if the initial cache query comes back empty
      val findInDbThenUpdateCache = for {
        stat <- findInDatabase(race, actionTaken)
        _ <- updateCache(race, actionTaken, stat)
      } yield stat

      for {
        optC <- findInCache(race, actionTaken)
        stat <- optC.fold(ifEmpty = findInDbThenUpdateCache)(cachedStat =>
          Task(cachedStat))
      } yield Aggregation(count = stat.count, sum = stat.sum, race, actionTaken)
    }

    Task.gatherUnordered(taskList)
  }

  override def fetchAggregate(
                               msaMd: MsaMd,
                               races: Seq[Race],
                               actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    def findDb(r: Race, a: ActionTaken): Task[Statistic] =
      repo.findAndAggregate(msaMd.msaMd, a.value, r.entryName)

    def findCache(r: Race, a: ActionTaken): Task[Option[Statistic]] =
      cache.find(msaMd.msaMd, a.value, r.entryName)

    def updateCache(r: Race, a: ActionTaken, s: Statistic): Task[Statistic] =
      cache.update(msaMd.msaMd, a.value, r.entryName, s)

    fetchAgg(races, actionsTaken, findDb, findCache, updateCache)
  }

  override def fetchAggregate(
                               state: State,
                               races: Seq[Race],
                               actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    def findDb(r: Race, a: ActionTaken): Task[Statistic] =
      repo.findAndAggregate(state.entryName, a.value, r.entryName)

    def findCache(r: Race, a: ActionTaken): Task[Option[Statistic]] =
      cache.find(state.entryName, a.value, r.entryName)

    def updateCache(r: Race, a: ActionTaken, s: Statistic): Task[Statistic] =
      cache.update(state.entryName, a.value, r.entryName, s)

    fetchAgg(races, actionsTaken, findDb, findCache, updateCache)
  }

  override def fetchAggregate(
                               races: Seq[Race],
                               actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    def findInDb(r: Race, a: ActionTaken): Task[Statistic] =
      repo.findAndAggregate(a.value, r.entryName)

    def findInCache(r: Race, a: ActionTaken): Task[Option[Statistic]] =
      cache.find(a.value, r.entryName)

    def updateCache(r: Race, a: ActionTaken, s: Statistic): Task[Statistic] =
      cache.update(a.value, r.entryName, s)

    fetchAgg(races, actionsTaken, findInDb, findInCache, updateCache)
  }

  // TODO: add headers for the CSV stream
  override def fetchData(
                          msaMd: MsaMd,
                          races: Seq[Race],
                          actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed] =
    repo.find(msaMd = msaMd.msaMd,
      actionsTaken = actionsTaken.map(_.value),
      races = races.map(_.entryName))

  override def fetchData(
                          state: State,
                          races: Seq[Race],
                          actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed] =
    repo.find(state = state.entryName,
      actionsTaken = actionsTaken.map(_.value),
      races = races.map(_.entryName))

  override def fetchData(
                          races: Seq[Race],
                          actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed] =
    repo.find(actionsTaken = actionsTaken.map(_.value),
      races = races.map(_.entryName))
}