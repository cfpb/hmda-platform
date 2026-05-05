package hmda.dataBrowser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import monix.eval.Task

trait ModifiedLarRepository[A, B] {
  def find(browserFields: List[QueryField], year: Int): Source[A, NotUsed]
  def findAndAggregate(instQueryField: QueryField, geoQueryField: QueryField, hmdaQueryFields: List[LarQueryField], year: Int): Task[Statistic]
  def findFilers(leiFields: List[QueryField], year: Int): Task[Seq[B]]
}

trait ModifiedLarRepository2017 extends ModifiedLarRepository[ModifiedLarEntity2017, FilerInformation2017]

trait ModifiedLarRepositoryLatest extends ModifiedLarRepository[ModifiedLarEntity, FilerInformationLatest]
