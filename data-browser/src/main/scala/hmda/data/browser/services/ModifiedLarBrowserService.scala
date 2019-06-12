package hmda.data.browser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._
import hmda.data.browser.repositories.{
  ModifiedLarAggregateCache,
  ModifiedLarRepository
}
import monix.eval.Task

class ModifiedLarBrowserService(repo: ModifiedLarRepository)
    extends BrowserService {

  //  /**
  //    * This is responsible for performing the following logic for all provided combinations of races and actions taken:
  //    *   1. find if the entry is in the cache
  //    *   2a. if the entry is in the cache, proceed to 3
  //    *   2b. if the entry is not in the cache then obtain it from the database and update the cache
  //    *   3. tag entry with race and action taken information
  //    *   4. serve all entries
  //    *
  //    * This function relies on callers of the function to use currying to cover all use cases:
  //    * - queries by MSAMD
  //    * - queries by State
  //    * - nationwide queries
  //    *
  //    * @param findInDatabase is a function that retrieves data from the database
  //    * @param findInCache is a function that retrieves data from the cache (the data may not be in the cache)
  //    * @param updateCache is a function that updates the cache with the data from the database
  //    * @return
  //    */
  //  private def fetchAgg(
  //      field1: BrowserField,
  //      field2: BrowserField,
  //      findInDatabase: (String, String, String, String) => Task[Statistic],
  //      findInCache: (String, String, String, String) => Task[Option[Statistic]],
  //      updateCache: (String,
  //                    String,
  //                    String,
  //                    String,
  //                    Statistic) => Task[Statistic]): Task[Seq[Aggregation]] = {
  //    val taskList = for {
  //      field1Entry <- field1.value
  //      field2Entry <- field2.value
  //    } yield {
  //      // this is only executed if the initial cache query comes back empty
  //      val findInDbThenUpdateCache = for {
  //        stat <- findInDatabase(field1.dbName,
  //                               field1Entry,
  //                               field2.dbName,
  //                               field2Entry)
  //        _ <- updateCache(field1.redisName,
  //                         field1Entry,
  //                         field2.redisName,
  //                         field2Entry,
  //                         stat)
  //      } yield stat
  //
  //      for {
  //        optC <- findInCache(field1.redisName,
  //                            field1Entry,
  //                            field2.redisName,
  //                            field2Entry)
  //        stat <- optC.fold(ifEmpty = findInDbThenUpdateCache)(cachedStat =>
  //          Task(cachedStat))
  //      } yield
  //        Aggregation(
  //          count = stat.count,
  //          sum = stat.sum,
  //          BrowserField(field1.name,
  //                       Seq(field1Entry),
  //                       field1.dbName,
  //                       field1.redisName),
  //          BrowserField(field2.name,
  //                       Seq(field2Entry),
  //                       field2.dbName,
  //                       field2.redisName)
  //        )
  //    }
  //
  //    Task.gatherUnordered(taskList)
  //  }

  //  override def fetchAggregate(msaMd: MsaMd,
  //                              field1: BrowserField,
  //                              field2: BrowserField): Task[Seq[Aggregation]] = {
  //    def findDb(oneName: String,
  //               one: String,
  //               twoName: String,
  //               two: String): Task[Statistic] =
  //      repo.findAndAggregate(msaMd.msaMd, oneName, one, twoName, two)
  //
  //    def findCache(oneName: String,
  //                  one: String,
  //                  twoName: String,
  //                  two: String): Task[Option[Statistic]] =
  //      cache.find(msaMd.msaMd, oneName, one, twoName, two)
  //
  //    def updateCache(oneName: String,
  //                    one: String,
  //                    twoName: String,
  //                    two: String,
  //                    s: Statistic): Task[Statistic] =
  //      cache.update(msaMd.msaMd, oneName, one, twoName, two, s)
  //
  //    fetchAgg(field1, field2, findDb, findCache, updateCache)
  //  }
  //
  //  override def fetchAggregate(state: State,
  //                              field1: BrowserField,
  //                              field2: BrowserField): Task[Seq[Aggregation]] = {
  //    def findDb(oneName: String,
  //               one: String,
  //               twoName: String,
  //               two: String): Task[Statistic] =
  //      repo.findAndAggregate(state.entryName, oneName, one, twoName, two)
  //
  //    def findCache(oneName: String,
  //                  one: String,
  //                  twoName: String,
  //                  two: String): Task[Option[Statistic]] =
  //      cache.find(state.entryName, oneName, one, twoName, two)
  //
  //    def updateCache(oneName: String,
  //                    one: String,
  //                    twoName: String,
  //                    two: String,
  //                    s: Statistic): Task[Statistic] =
  //      cache.update(state.entryName, oneName, one, twoName, two, s)
  //
  //    fetchAgg(field1, field2, findDb, findCache, updateCache)
  //  }
  //
  //  override def fetchAggregate(field1: BrowserField,
  //                              field2: BrowserField): Task[Seq[Aggregation]] = {
  //    def findInDb(oneName: String,
  //                 one: String,
  //                 twoName: String,
  //                 two: String): Task[Statistic] =
  //      repo.findAndAggregate(oneName, one, twoName, two)
  //
  //    def findInCache(oneName: String,
  //                    one: String,
  //                    twoName: String,
  //                    two: String): Task[Option[Statistic]] =
  //      cache.find(oneName, one, twoName, two)
  //
  //    def updateCache(oneName: String,
  //                    one: String,
  //                    twoName: String,
  //                    two: String,
  //                    s: Statistic): Task[Statistic] =
  //      cache.update(oneName, one, twoName, two, s)
  //
  //    fetchAgg(field1, field2, findInDb, findInCache, updateCache)
  //  }
  //
  //  override def fetchAggregate(msaMd: MsaMd,
  //                              state: State,
  //                              field1: BrowserField,
  //                              field2: BrowserField): Task[Seq[Aggregation]] = {
  //    def findDb(oneName: String,
  //               one: String,
  //               twoName: String,
  //               two: String): Task[Statistic] =
  //      repo.findAndAggregate(msaMd.msaMd,
  //                            state.entryName,
  //                            oneName,
  //                            one,
  //                            twoName,
  //                            two)
  //
  //    def findCache(oneName: String,
  //                  one: String,
  //                  twoName: String,
  //                  two: String): Task[Option[Statistic]] =
  //      cache.find(msaMd.msaMd, state.entryName, oneName, one, twoName, two)
  //
  //    def updateCache(oneName: String,
  //                    one: String,
  //                    twoName: String,
  //                    two: String,
  //                    s: Statistic): Task[Statistic] =
  //      cache.update(msaMd.msaMd, state.entryName, oneName, one, twoName, two, s)
  //
  //    fetchAgg(field1, field2, findDb, findCache, updateCache)
  //  }

  override def fetchData(
      browserFields: List[QueryField]): Source[ModifiedLarEntity, NotUsed] =
    repo.find(browserFields)

  private def generateCombinations[T](x: List[List[T]]): List[List[T]] = {
    x match {
      case Nil    => List(Nil)
      case h :: _ => h.flatMap(i => generateCombinations(x.tail).map(i :: _))
    }
  }

  def permuteQueryFields(input: List[QueryField]): List[List[QueryField]] = {
    val singleElementBrowserFields: List[List[QueryField]] =
      input.map {
        case QueryField(name, values, dbName) =>
          values
            .map(value => QueryField(name, value :: Nil, dbName))
            .toList
      }
    generateCombinations(singleElementBrowserFields)
  }

  override def fetchAggregate(
      fields: List[QueryField]): Task[Seq[Aggregation]] = {
    val optState: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "state")
    val optMsaMd: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "msamd")
    val rest = fields.filterNot(_.name == "state").filterNot(_.name == "msamd")

    val queryFieldCombinations = permuteQueryFields(rest).map(eachList =>
      optState.toList ++ optMsaMd.toList ++ eachList)

    Task.gatherUnordered {
      queryFieldCombinations.map { eachCombination =>
        println(eachCombination)
        val fieldInfos =
          eachCombination.map(field =>
            FieldInfo(field.name, field.values.mkString(",")))
        repo
          .findAndAggregate(eachCombination)
          .map(statistic =>
            Aggregation(statistic.count, statistic.sum, fieldInfos))
      }
    }
  }
}
