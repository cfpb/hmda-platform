package hmda.data.browser.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.data.browser.models._

import monix.eval.Task

trait BrowserService {
  def fetchAggregate(msaMd: MsaMd,
                     field1: BrowserField,
                     field2: BrowserField): Task[Seq[Aggregation]]

  def fetchData(msaMd: MsaMd,
                field1: BrowserField,
                field2: BrowserField): Source[ModifiedLarEntity, NotUsed]

  def fetchAggregate(state: State,
                     field1: BrowserField,
                     field2: BrowserField): Task[Seq[Aggregation]]

  def fetchData(state: State,
                field1: BrowserField,
                field2: BrowserField): Source[ModifiedLarEntity, NotUsed]

  def fetchAggregate(field1: BrowserField,
                     field2: BrowserField): Task[Seq[Aggregation]]

  def fetchData(field1: BrowserField,
                field2: BrowserField): Source[ModifiedLarEntity, NotUsed]

  def fetchAggregate(msaMd: MsaMd,
                     state: State,
                     field1: BrowserField,
                     field2: BrowserField): Task[Seq[Aggregation]]

  def fetchData(msaMd: MsaMd,
                state: State,
                field1: BrowserField,
                field2: BrowserField): Source[ModifiedLarEntity, NotUsed]
}
