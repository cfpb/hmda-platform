package hmda.dataBrowser.services

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import cats.implicits._
import hmda.dataBrowser.models.HealthCheckStatus._
import hmda.dataBrowser.models.State._
import hmda.dataBrowser.models.{
  Commas,
  HealthCheckResponse,
  HealthCheckStatus,
  QueryField
}
import hmda.dataBrowser.repositories.{
  ModifiedLarAggregateCache,
  ModifiedLarRepository
}
import monix.eval.Task

class HealthCheckService(
    database: ModifiedLarRepository,
    cache: ModifiedLarAggregateCache,
    storage: FileService)(implicit mat: ActorMaterializer) {
  private def health[A](task: Task[A]): Task[HealthCheckStatus] =
    task.as(Up).onErrorFallbackTo(Task.pure(Down))

  def healthCheckStatus: Task[HealthCheckResponse] = {
    val exampleQuery =
      List(
        QueryField(name = "year",
                   values = List("2018"),
                   dbName = "filing_year"),
        QueryField(name = "state",
                   values = List(AK.entryName),
                   dbName = "state")
      )

    def databaseQuery: Task[HealthCheckStatus] =
      health {
        Task.deferFuture(database.find(exampleQuery).runWith(Sink.headOption))
      }

    def cacheQuery: Task[HealthCheckStatus] =
      health {
        cache.find(exampleQuery)
      }

    def storageQuery: Task[HealthCheckStatus] =
      health {
        storage.retrieveDataUrl(exampleQuery, Commas)
      }

    Task.parMap3(databaseQuery, cacheQuery, storageQuery) {
      (db, cache, storage) =>
        HealthCheckResponse(cache, db, storage)
    }
  }
}
