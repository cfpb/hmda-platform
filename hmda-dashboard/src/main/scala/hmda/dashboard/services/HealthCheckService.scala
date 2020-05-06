package hmda.dashboard.services

import cats.implicits._
import hmda.dashboard.models.HealthCheckStatus._
import hmda.dashboard.models.{ HealthCheckResponse, HealthCheckStatus }
import hmda.dashboard.repositories.PostgresRepository
import monix.eval.Task

class HealthCheckService(database: PostgresRepository) {
  private def health[A](task: Task[A]): Task[HealthCheckStatus] =
    task.as(Up).onErrorFallbackTo(Task.pure(Down))

  def healthCheckStatus: Task[HealthCheckResponse] =
    health(database.healthCheck).map(HealthCheckResponse)
}