package hmda.data.browser.repositories

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._
import monix.eval.Task

trait ModifiedLarRepository {
  def find(msaMd: Int,
           state: String,
           field1: BrowserField,
           field2: BrowserField): Source[ModifiedLarEntity, NotUsed]

  def find(msaMd: Int,
           field1: BrowserField,
           field2: BrowserField): Source[ModifiedLarEntity, NotUsed]

  def find(state: String,
           field1: BrowserField,
           field2: BrowserField): Source[ModifiedLarEntity, NotUsed]

  def find(field1: BrowserField,
           field2: BrowserField): Source[ModifiedLarEntity, NotUsed]

  def findAndAggregate(msaMd: Int,
                       state: String,
                       field1: String,
                       field1DbName: String,
                       field2: String,
                       field2DbName: String): Task[Statistic]

  def findAndAggregate(msaMd: Int,
                       field1: String,
                       field1DbName: String,
                       field2: String,
                       field2DbName: String): Task[Statistic]

  def findAndAggregate(state: String,
                       field1: String,
                       field1DbName: String,
                       field2: String,
                       field2DbName: String): Task[Statistic]

  def findAndAggregate(field1: String,
                       field1DbName: String,
                       field2: String,
                       field2DbName: String): Task[Statistic]
}
