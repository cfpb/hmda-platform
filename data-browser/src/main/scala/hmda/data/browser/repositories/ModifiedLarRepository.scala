package hmda.data.browser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._
import monix.eval.Task

trait ModifiedLarRepository {
  def find(
            browserFields: List[BrowserField]): Source[ModifiedLarEntity, NotUsed]
  def findAndAggregate(browserFields: List[BrowserField]): Task[Statistic]
}