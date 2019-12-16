package hmda.dashboard.services

import hmda.dashboard.models._
import hmda.dashboard.repositories.PostgresRepository
import monix.eval.Task

class DashboardQueryService (repo: PostgresRepository) extends QueryService{
    override def fetchTotalFilers(year: Int): Task[Seq[TotalFilers]] =
    repo.fetchTotalFilers(year)

  override def fetchTotalLars(year: Int): Task[Seq[TotalLars]] =
    repo.fetchTotalLars(year)

  override def fetchSingleAttempts(year: Int): Task[Seq[SingleAttempts]] =
    repo.fetchSingleAttempts(year)

  override def fetchMultipleAttempts(year: Int): Task[Seq[MultipleAttempts]] =
    repo.fetchMultipleAttempts(year)

}
