package hmda.dashboard.services

import hmda.dashboard.models.{MultipleAttempts, SingleAttempts, TotalFilers, TotalLars}
import monix.eval.Task

trait QueryService {
  def fetchTotalFilers(year: Int): Task[Seq[TotalFilers]]
  def fetchTotalLars(year: Int): Task[Seq[TotalLars]]
  def fetchSingleAttempts(year: Int): Task[Seq[SingleAttempts]]
  def fetchMultipleAttempts(year: Int): Task[Seq[MultipleAttempts]]
}
