package hmda.data.browser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._
import hmda.data.browser.repositories.{
  ModifiedLarAggregateCache,
  ModifiedLarRepository
}
import monix.eval.Task

class ModifiedLarBrowserService(repo: ModifiedLarRepository,
                                cache: ModifiedLarAggregateCache)
    extends BrowserService {
  override def fetchAggregate(
      msaMd: MsaMd,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    val taskList = for {
      race <- races
      action <- actionsTaken
    } yield {
      // description of finding the entry in the database and then performing a cache update
      // this will only be executed if the entry is not in the cache
      val findDbUpdateCache = for {
        stat <- repo.findAndAggregate(msaMd.msaMd, action.value, race.entryName)
        _ <- cache.update(msaMd.msaMd, action.value, race.entryName, stat)
      } yield stat

      for {
        optStat <- cache.find(msaMd.msaMd, action.value, race.entryName)
        stat <- optStat.fold(ifEmpty = findDbUpdateCache)(cachedStat =>
          Task(cachedStat))
      } yield Aggregation(stat.count, stat.sum, race, action)
    }

    Task.gatherUnordered(taskList)
  }

  override def fetchAggregate(
      state: State,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    val taskList = for {
      race <- races
      action <- actionsTaken
    } yield {
      // description of finding the entry in the database and then performing a cache update
      // this will only be executed if the entry is not in the cache
      val findDbUpdateCache = for {
        stat <- repo.findAndAggregate(state.entryName,
                                      action.value,
                                      race.entryName)
        _ <- cache.update(state.entryName, action.value, race.entryName, stat)
      } yield stat

      for {
        optStat <- cache.find(state.entryName, action.value, race.entryName)
        stat <- optStat.fold(ifEmpty = findDbUpdateCache)(cachedStat =>
          Task(cachedStat))
      } yield Aggregation(stat.count, stat.sum, race, action)
    }

    Task.gatherUnordered(taskList)
  }

  override def fetchAggregate(
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    val taskList = for {
      race <- races
      action <- actionsTaken
    } yield {
      // description of finding the entry in the database and then performing a cache update
      // this will only be executed if the entry is not in the cache
      val findDbUpdateCache = for {
        stat <- repo.findAndAggregate(action.value, race.entryName)
        _ <- cache.update(action.value, race.entryName, stat)
      } yield stat

      for {
        optStat <- cache.find(action.value, race.entryName)
        stat <- optStat.fold(ifEmpty = findDbUpdateCache)(cachedStat =>
          Task(cachedStat))
      } yield Aggregation(stat.count, stat.sum, race, action)
    }

    Task.gatherUnordered(taskList)
  }

  override def fetchData(
      msaMd: MsaMd,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed] = {
    val list = for {
      race <- races
      action <- actionsTaken
    } yield repo.find(msaMd.msaMd, action.value, race.entryName)
    list.reduce((s1, s2) => s2 prepend s1)
  }

  override def fetchData(
      state: State,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed] = {
    val list = for {
      race <- races
      action <- actionsTaken
    } yield repo.find(state.entryName, action.value, race.entryName)
    list.reduce((s1, s2) => s2 prepend s1)
  }

  override def fetchData(
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed] = {
    val list = for {
      race <- races
      action <- actionsTaken
    } yield repo.find(action.value, race.entryName)
    list.reduce((s1, s2) => s2 prepend s1)
  }
}
