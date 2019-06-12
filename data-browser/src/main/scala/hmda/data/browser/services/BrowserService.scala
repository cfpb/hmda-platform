package hmda.data.browser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._

import monix.eval.Task

trait BrowserService {
  def fetchAggregate(fields: List[BrowserField]): Task[Seq[Aggregation]]
  def fetchData(fields: List[BrowserField]): Source[ModifiedLarEntity, NotUsed]
}
