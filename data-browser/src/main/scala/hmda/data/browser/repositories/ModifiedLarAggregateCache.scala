package hmda.data.browser.repositories

import hmda.data.browser.models.QueryField
import monix.eval.Task

trait ModifiedLarAggregateCache {
  def find(queryFields: List[QueryField]): Task[Option[Statistic]]

  def update(queryFields: List[QueryField],
             statistic: Statistic): Task[Statistic]

  def invalidate(queryField: List[QueryField]): Task[Unit]
}