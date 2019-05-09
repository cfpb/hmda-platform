package hmda.data.browser.repositories

import monix.eval.Task

trait ModifiedLarAggregateCache {
  def find(msaMd: Int, actionTaken: Int, race: String): Task[Option[Statistic]]

  def find(state: String,
           actionTaken: Int,
           race: String): Task[Option[Statistic]]

  def find(actionTaken: Int, race: String): Task[Option[Statistic]]

  def find(msaMd: Int,
           state: String,
           actionTaken: Int,
           race: String): Task[Option[Statistic]]

  def update(msaMd: Int,
             actionTaken: Int,
             race: String,
             stat: Statistic): Task[Statistic]

  def update(state: String,
             actionTaken: Int,
             race: String,
             stat: Statistic): Task[Statistic]

  def update(actionTaken: Int, race: String, stat: Statistic): Task[Statistic]

  def update(msaMd: Int,
             state: String,
             actionTaken: Int,
             race: String,
             stat: Statistic): Task[Statistic]

  def invalidate(msaMd: Int, actionTaken: Int, race: String): Task[Unit]

  def invalidate(state: String, actionTaken: Int, race: String): Task[Unit]

  def invalidate(actionTaken: Int, race: String): Task[Unit]

  def invalidate(msaMd: Int,
                 state: String,
                 actionTaken: Int,
                 race: String): Task[Unit]
}
