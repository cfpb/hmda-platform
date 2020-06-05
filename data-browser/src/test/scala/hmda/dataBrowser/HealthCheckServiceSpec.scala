package hmda.dataBrowser

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.testkit.TestKit
import hmda.dataBrowser.models.HealthCheckStatus.Up
import hmda.dataBrowser.models.{ Commas, HealthCheckResponse, QueryField }
import hmda.dataBrowser.models.State.AK
import hmda.dataBrowser.repositories.{ PostgresModifiedLarRepository, RedisModifiedLarAggregateCache }
import hmda.dataBrowser.services.{ HealthCheckService, S3FileService }
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest.concurrent.ScalaFutures

class HealthCheckServiceSpec
  extends TestKit(ActorSystem("health-check-service-spec"))
    with WordSpecLike
    with MockFactory
    with ScalaFutures
    with Matchers {

  implicit val mat: Materializer              = Materializer(system)
  implicit val scheduler: TestScheduler       = TestScheduler(ExecutionModel.SynchronousExecution)
  val database: PostgresModifiedLarRepository = mock[PostgresModifiedLarRepository]
  val cache: RedisModifiedLarAggregateCache   = mock[RedisModifiedLarAggregateCache]
  val storage: S3FileService                  = mock[S3FileService]
  val health                                  = new HealthCheckService(database, cache, storage)

  "HealthService" must {
    "respond with OK if all three services are up" in {
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

      (database.healthCheck _).expects().returns(Task.unit)
      (cache.healthCheck _).expects().returns(Task.unit)
      (storage.retrieveDataUrl _).expects(exampleQuery, Commas, "2018").returns(Task.now(None))

      val futHealth = health.healthCheckStatus.runToFuture
      scheduler.tick()
      whenReady(futHealth)(_ shouldBe HealthCheckResponse(Up, Up, Up))
    }
  }
}