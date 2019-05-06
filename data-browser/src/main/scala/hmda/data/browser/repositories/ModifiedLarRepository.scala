package hmda.data.browser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models.ModifiedLarEntity
import monix.eval.Task

trait ModifiedLarRepository {
  def find(msaMd: Int,
           actionTaken: Int,
           race: String): Source[ModifiedLarEntity, NotUsed]

  def find(state: String,
           actionTaken: Int,
           race: String): Source[ModifiedLarEntity, NotUsed]

  def find(actionTaken: Int, race: String): Source[ModifiedLarEntity, NotUsed]

  def findAndAggregate(msaMd: Int,
                       actionTaken: Int,
                       race: String): Task[Statistic]

  def findAndAggregate(state: String,
                       actionTaken: Int,
                       race: String): Task[Statistic]

  def findAndAggregate(actionTaken: Int, race: String): Task[Statistic]
}
