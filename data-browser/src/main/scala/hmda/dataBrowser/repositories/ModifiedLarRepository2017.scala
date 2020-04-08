package hmda.dataBrowser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import monix.eval.Task

trait ModifiedLarRepository2017 {
  def find(browserFields: List[QueryField]): Source[ModifiedLarEntity2017, NotUsed]
  def findAndAggregate(browserFields: List[QueryField]): Task[Statistic]
  def findFilers(leiFields: List[QueryField]): Task[Seq[FilerInformation]]
}
