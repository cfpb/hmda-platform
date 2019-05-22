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
      field1: BrowserField,
      field2: BrowserField,
      findInDatabase: (String, String, String, String) => Task[Statistic],
      findInCache: (String, String, String, String) => Task[Option[Statistic]],
      updateCache: (String,
                    String,
                    String,
                    String,
                    Statistic) => Task[Statistic]): Task[Seq[Aggregation]] = {
    val taskList = for {
      field1Entry <- field1.value
      field2Entry <- field2.value
    } yield {
      // this is only executed if the initial cache query comes back empty
      val findInDbThenUpdateCache = for {
        stat <- findInDatabase(field1.dbName,
                               field1Entry,
                               field2.dbName,
                               field2Entry)
        _ <- updateCache(field1.redisName,
                         field1Entry,
                         field2.redisName,
                         field2Entry,
                         stat)
      } yield stat

      for {
        optC <- findInCache(field1.redisName,
                            field1Entry,
                            field2.redisName,
                            field2Entry)
        stat <- optC.fold(ifEmpty = findInDbThenUpdateCache)(cachedStat =>
          Task(cachedStat))
      } yield Aggregation(count = stat.count, sum = stat.sum, field1, field2)
    }

    Task.gatherUnordered(taskList)
  }

  override def fetchAggregate(msaMd: MsaMd,
                              field1: BrowserField,
                              field2: BrowserField): Task[Seq[Aggregation]] = {
    def findDb(oneName: String,
               one: String,
               twoName: String,
               two: String): Task[Statistic] =
      repo.findAndAggregate(msaMd.msaMd, oneName, one, twoName, two)

    def findCache(oneName: String,
                  one: String,
                  twoName: String,
                  two: String): Task[Option[Statistic]] =
      cache.find(msaMd.msaMd, oneName, one, twoName, two)

    def updateCache(oneName: String,
                    one: String,
                    twoName: String,
                    two: String,
                    s: Statistic): Task[Statistic] =
      cache.update(msaMd.msaMd, oneName, one, twoName, two, s)

    fetchAgg(field1, field2, findDb, findCache, updateCache)
  }

  override def fetchAggregate(state: State,
                              field1: BrowserField,
                              field2: BrowserField): Task[Seq[Aggregation]] = {
    def findDb(oneName: String,
               one: String,
               twoName: String,
               two: String): Task[Statistic] =
      repo.findAndAggregate(state.entryName, oneName, one, twoName, two)

    def findCache(oneName: String,
                  one: String,
                  twoName: String,
                  two: String): Task[Option[Statistic]] =
      cache.find(state.entryName, oneName, one, twoName, two)

    def updateCache(oneName: String,
                    one: String,
                    twoName: String,
                    two: String,
                    s: Statistic): Task[Statistic] =
      cache.update(state.entryName, oneName, one, twoName, two, s)

    fetchAgg(field1, field2, findDb, findCache, updateCache)
  }

  override def fetchAggregate(field1: BrowserField,
                              field2: BrowserField): Task[Seq[Aggregation]] = {
    def findInDb(oneName: String,
                 one: String,
                 twoName: String,
                 two: String): Task[Statistic] =
      repo.findAndAggregate(oneName, one, twoName, two)

    def findInCache(oneName: String,
                    one: String,
                    twoName: String,
                    two: String): Task[Option[Statistic]] =
      cache.find(oneName, one, twoName, two)

    def updateCache(oneName: String,
                    one: String,
                    twoName: String,
                    two: String,
                    s: Statistic): Task[Statistic] =
      cache.update(oneName, one, twoName, two, s)

    fetchAgg(field1, field2, findInDb, findInCache, updateCache)
  }

  override def fetchAggregate(msaMd: MsaMd,
                              state: State,
                              field1: BrowserField,
                              field2: BrowserField): Task[Seq[Aggregation]] = {
    def findDb(oneName: String,
               one: String,
               twoName: String,
               two: String): Task[Statistic] =
      repo.findAndAggregate(msaMd.msaMd,
                            state.entryName,
                            oneName,
                            one,
                            twoName,
                            two)

    def findCache(oneName: String,
                  one: String,
                  twoName: String,
                  two: String): Task[Option[Statistic]] =
      cache.find(msaMd.msaMd, state.entryName, oneName, one, twoName, two)

    def updateCache(oneName: String,
                    one: String,
                    twoName: String,
                    two: String,
                    s: Statistic): Task[Statistic] =
      cache.update(msaMd.msaMd, state.entryName, oneName, one, twoName, two, s)

    fetchAgg(field1, field2, findDb, findCache, updateCache)
  }

  override def fetchData(
      msaMd: MsaMd,
      field1: BrowserField,
      field2: BrowserField): Source[ModifiedLarEntity, NotUsed] =
    repo.find(msaMd = msaMd.msaMd, field1, field2)

  override def fetchData(
      state: State,
      field1: BrowserField,
      field2: BrowserField): Source[ModifiedLarEntity, NotUsed] =
    repo.find(state = state.entryName, field1, field2)

  override def fetchData(
      field1: BrowserField,
      field2: BrowserField): Source[ModifiedLarEntity, NotUsed] =
    repo.find(field1, field2)

  override def fetchData(
      msaMd: MsaMd,
      state: State,
      field1: BrowserField,
      field2: BrowserField): Source[ModifiedLarEntity, NotUsed] =
    repo.find(msaMd = msaMd.msaMd, state = state.entryName, field1, field2)
}
