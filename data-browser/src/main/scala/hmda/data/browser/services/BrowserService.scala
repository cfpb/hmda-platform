package hmda.data.browser.services

import hmda.data.browser.models.{ActionTaken, Aggregation,MsaMd, Race, State}
import monix.eval.Task

 trait BrowserService {
  def fetchAggregate(msaMd: MsaMd,
                     races: Seq[Race],
                     actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]]

   def fetchAggregate(state: State,
                     races: Seq[Race],
                     actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]]

  def fetchAggregate(races: Seq[Race],
                     actionsTaken: Seq[ActionTaken]): Task[Seq[Aggregation]]   
}