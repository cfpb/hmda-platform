package hmda.dataBrowser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import monix.eval.Task

trait ModifiedLarRepository[A] {
  def find(browserFields: List[QueryField]): Source[A, NotUsed]
  def findAndAggregate(browserFields: List[QueryField]): Task[Statistic]
  def findFilers(leiFields: List[QueryField]): Task[Seq[FilerInformation]]
}

trait ModifiedLarRepository2017 extends ModifiedLarRepository[ModifiedLarEntity2017] 

trait ModifiedLarRepository2018 extends ModifiedLarRepository[ModifiedLarEntity] 