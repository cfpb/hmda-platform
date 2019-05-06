package hmda.data.browser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._
import hmda.data.browser.repositories.ModifiedLarRepository
import monix.eval.Task

class ModifiedLarBrowserService(repo: ModifiedLarRepository)
    extends BrowserService {
  override def fetchAggregate(
      msaMd: MsaMd,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    val taskList = for {
      race <- races
      action <- actionsTaken
    } yield
      repo
        .findAndAggregate(msaMd.msaMd, action.value, race.entryName)
        .map(
          stat => Aggregation(stat.count, stat.sum, race, action)
        )

    Task.gatherUnordered(taskList)
  }

  override def fetchAggregate(
      state: State,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    val taskList = for {
      race <- races
      action <- actionsTaken
    } yield
      repo
        .findAndAggregate(state.entryName, action.value, race.entryName)
        .map(
          stat => Aggregation(stat.count, stat.sum, race, action)
        )

    Task.gatherUnordered(taskList)
  }

  override def fetchAggregate(
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]] = {
    val taskList = for {
      race <- races
      action <- actionsTaken
    } yield
      repo
        .findAndAggregate(action.value, race.entryName)
        .map(
          stat => Aggregation(stat.count, stat.sum, race, action)
        )

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