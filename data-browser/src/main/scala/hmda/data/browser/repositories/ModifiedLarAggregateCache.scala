package hmda.data.browser.repositories

import monix.eval.Task

trait ModifiedLarAggregateCache {
  def find(msaMd: Int,
           field1Name: String,
           field1: String,
           field2Name: String,
           field2: String): Task[Option[Statistic]]

  def find(state: String,
           field1Name: String,
           field1: String,
           field2Name: String,
           field2: String): Task[Option[Statistic]]

  def find(field1Name: String,
           field1: String,
           field2Name: String,
           field2: String): Task[Option[Statistic]]

  def find(msaMd: Int,
           state: String,
           field1Name: String,
           field1: String,
           field2Name: String,
           field2: String): Task[Option[Statistic]]

  def update(msaMd: Int,
             field1Name: String,
             field1: String,
             field2Name: String,
             field2: String,
             stat: Statistic): Task[Statistic]

  def update(state: String,
             field1Name: String,
             field1: String,
             field2Name: String,
             field2: String,
             stat: Statistic): Task[Statistic]

  def update(field1Name: String,
             field1: String,
             field2Name: String,
             field2: String,
             stat: Statistic): Task[Statistic]

  def update(msaMd: Int,
             state: String,
             field1Name: String,
             field1: String,
             field2Name: String,
             field2: String,
             stat: Statistic): Task[Statistic]

  def invalidate(msaMd: Int,
                 field1Name: String,
                 field1: String,
                 field2Name: String,
                 field2: String): Task[Unit]

  def invalidate(state: String,
                 field1Name: String,
                 field1: String,
                 field2Name: String,
                 field2: String): Task[Unit]

  def invalidate(field1Name: String,
                 field1: String,
                 field2Name: String,
                 field2: String): Task[Unit]

  def invalidate(msaMd: Int,
                 state: String,
                 field1Name: String,
                 field1: String,
                 field2Name: String,
                 field2: String): Task[Unit]
}
