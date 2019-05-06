package hmda.data.browser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models.{
  ActionTaken,
  Aggregation,
  ModifiedLarEntity,
  MsaMd,
  Race,
  State
}
import monix.eval.Task

trait BrowserService {
  def fetchAggregate(msaMd: MsaMd,
                     races: Seq[Race],
                     actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]]

  def fetchData(
      msaMd: MsaMd,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed]

  def fetchAggregate(state: State,
                     races: Seq[Race],
                     actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]]

  def fetchData(
      state: State,
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed]

  def fetchAggregate(races: Seq[Race],
                     actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]]

  def fetchData(
      races: Seq[Race],
      actionsTaken: Seq[ActionTaken]): Source[ModifiedLarEntity, NotUsed]
}
