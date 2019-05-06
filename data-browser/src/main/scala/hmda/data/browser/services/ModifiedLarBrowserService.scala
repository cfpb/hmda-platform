package hmda.data.browser.services
import hmda.data.browser.models.{ActionTaken, Aggregation, MsaMd, Race, State}
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

}