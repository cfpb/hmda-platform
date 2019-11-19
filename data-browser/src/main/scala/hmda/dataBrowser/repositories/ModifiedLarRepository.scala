package hmda.dataBrowser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import monix.eval.Task

trait ModifiedLarRepository {
  def find(browserFields: List[QueryField]): Source[ModifiedLarEntity, NotUsed]
  def findAndAggregate(browserFields: List[QueryField]): Task[Statistic]
  def findFilers(leiFields: List[QueryField]): Task[Seq[FilerInformation]]
}
