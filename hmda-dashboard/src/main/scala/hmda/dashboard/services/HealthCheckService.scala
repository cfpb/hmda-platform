package hmda.dashboard.services

import akka.stream.ActorMaterializer
import cats.implicits._
import hmda.dashboard.models.HealthCheckStatus._
import hmda.dashboard.models.{ HealthCheckResponse, HealthCheckStatus}
import hmda.dashboard.repositories.{ PostgresRepository }
import monix.eval.Task

class HealthCheckService(
                          database: PostgresRepository,
                        )(implicit mat: ActorMaterializer) {
  private def health[A](task: Task[A]): Task[HealthCheckStatus] =
    task.as(Up).onErrorFallbackTo(Task.pure(Down))

  def healthCheckStatus: Task[HealthCheckResponse] = {

    def databaseQuery: Task[HealthCheckStatus] =
      health(database.healthCheck)

    Task.parMap2(databaseQuery, databaseQuery) { (db,db2) =>
      HealthCheckResponse(db)
    }

  }
}

