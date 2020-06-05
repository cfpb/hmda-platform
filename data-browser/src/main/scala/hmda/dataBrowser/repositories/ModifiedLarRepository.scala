package hmda.dataBrowser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import monix.eval.Task

trait ModifiedLarRepository[A, B] {
  def find(browserFields: List[QueryField]): Source[A, NotUsed]
  def findAndAggregate(browserFields: List[QueryField]): Task[Statistic]
  def findFilers(leiFields: List[QueryField]): Task[Seq[B]]
}

trait ModifiedLarRepository2017 extends ModifiedLarRepository[ModifiedLarEntity2017, FilerInformation2017] 

trait ModifiedLarRepository2018 extends ModifiedLarRepository[ModifiedLarEntity, FilerInformation2018] 