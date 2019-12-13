package hmda.dashboard.services

import hmda.dashboard.models._
import hmda.dashboard.repositories.PostgresRepository
import monix.eval.Task

class DashboardQueryService (repo: PostgresRepository) extends QueryService{
    override def fetchTotalFilers(
                          year: Int
                        ): Task[Vector[TotalFilers]] =
    repo.fetchTotalFilers(year)

}
