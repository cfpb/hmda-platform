package hmda.data.browser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models.{ModifiedLarEntity, Statistic}

import scala.concurrent.Future

trait ModifiedLarRepository {
  def find(msaMd: Int,
           actionTaken: Int,
           race: String): Source[ModifiedLarEntity, NotUsed]

  def find(state: String,
           actionTaken: Int,
           race: String): Source[ModifiedLarEntity, NotUsed]

  def findAndAggregate(msaMd: Int,
                       actionTaken: Int,
                       race: String): Future[Statistic]

  def findAndAggregate(state: String,
                       actionTaken: Int,
                       race: String): Future[Statistic]
}