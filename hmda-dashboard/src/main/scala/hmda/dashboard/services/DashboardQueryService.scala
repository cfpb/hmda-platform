package hmda.dashboard.services

import hmda.dashboard.models._
import hmda.dashboard.repositories.PostgresRepository
import monix.eval.Task

class DashboardQueryService (repo: PostgresRepository) extends QueryService{
    override def fetchData(
                          year: Int
                        ): Task[Vector[TotalFilers]] =
    repo.find(year)

}
