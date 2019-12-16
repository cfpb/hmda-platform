package hmda.dashboard.services

import hmda.dashboard.models.{FilerAttempts, FilersForLastDays, MultipleAttempts, SingleAttempts, TopFilers, TotalFilers, TotalLars}
import monix.eval.Task

trait QueryService {
  def fetchTotalFilers(year: Int) : Task[Seq[TotalFilers]]
  def fetchTotalLars(year: Int) : Task[Seq[TotalLars]]
  def fetchSingleAttempts(year: Int) : Task[Seq[SingleAttempts]]
  def fetchMultipleAttempts(year: Int) : Task[Seq[MultipleAttempts]]
  def fetchTopFilers(count: Int, year: Int) : Task[Seq[TopFilers]]
  def fetchFilersForLastDays(days: Int, year: Int) : Task[Seq[FilersForLastDays]]
  def fetchFilerAttempts(count: Int, year: Int) : Task[Seq[FilerAttempts]]
}
