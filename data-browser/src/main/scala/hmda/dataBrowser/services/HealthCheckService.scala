package hmda.dataBrowser.services

import hmda.dataBrowser.models.HealthCheckStatus._
import hmda.dataBrowser.models.State._
import hmda.dataBrowser.models.{ Commas, HealthCheckResponse, HealthCheckStatus, QueryField }
import hmda.dataBrowser.repositories.{ PostgresModifiedLarRepository, RedisModifiedLarAggregateCache }
import monix.eval.Task

/**
 * This is a specific health check service
 * @param database
 * @param cache
 * @param storage
 */
class HealthCheckService(
                          database: PostgresModifiedLarRepository,
                          cache: RedisModifiedLarAggregateCache,
                          storage: S3FileService
                        ) {
  private def health[A](task: Task[A]): Task[HealthCheckStatus] =
    task.as(Up).onErrorFallbackTo(Task.pure(Down))

  def healthCheckStatus: Task[HealthCheckResponse] = {
    val exampleQuery =
      List(
        QueryField(
          name = "year",
          values = List("2018"),
          dbName = "filing_year"
        ),
        QueryField(
          name = "state",
          values = List(AK.entryName),
          dbName = "state"
        )
      )

    def databaseQuery: Task[HealthCheckStatus] =
      health(database.healthCheck)

    def cacheQuery: Task[HealthCheckStatus] =
      health(cache.healthCheck)

    def storageQuery: Task[HealthCheckStatus] =
      health {
        storage.retrieveDataUrl(exampleQuery, Commas, "2018")
      }

    Task.parMap3(databaseQuery, cacheQuery, storageQuery)((db, cache, storage) => HealthCheckResponse(cache, db, storage))
  }
}