package hmda.dataBrowser

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import hmda.dataBrowser.api.DataBrowserHttpApi
import hmda.dataBrowser.models.HealthCheckStatus.{ Down, Up }
import hmda.dataBrowser.models.{ AggregationResponse, HealthCheckResponse, Statistic }
import hmda.dataBrowser.repositories._
import hmda.dataBrowser.services.{ DataBrowserQueryService, HealthCheckService, S3FileService }
import hmda.utils.EmbeddedPostgres
import monix.eval.Task
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ Matchers, WordSpec }
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class DataBrowserIntegrationSpec
  extends WordSpec
    with ScalatestRouteTest
    with EmbeddedPostgres
    with MockFactory
    with Matchers
    with FailFastCirceSupport
    with Settings {
  val log = LoggerFactory.getLogger(classOf[DataBrowserIntegrationSpec])

  val fileStorage = mock[S3FileService]

  val repository2018 = {
    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("embedded-pg")
    new PostgresModifiedLarRepository(databaseConfig, database.tableSelector)
  }

  val repository2017 = {
    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("embedded-pg")
    new PostgresModifiedLarRepository2017(database.tableName2017, databaseConfig)
  }

  val cache: RedisModifiedLarAggregateCache = mock[RedisModifiedLarAggregateCache]

  val query = new DataBrowserQueryService(repository2018, repository2017, cache, log)

  val healthCheck: HealthCheckService = mock[HealthCheckService]

  val routes: Route = DataBrowserHttpApi.create(log, fileStorage, query, healthCheck)

  "Data Browser" must {
    "respond to health checks" in {
      (healthCheck.healthCheckStatus _).expects().returns(Task.now(HealthCheckResponse(Up, Up, Up)))
      Get("/health") ~> routes ~> check {
        response.status shouldBe StatusCodes.OK

      }
    }

    "respond to health checks when downstream dependencies are failing" in {
      (healthCheck.healthCheckStatus _).expects().returns(Task.now(HealthCheckResponse(Up, Down, Down)))
      Get("/health") ~> routes ~> check {
        response.status shouldBe StatusCodes.ServiceUnavailable
      }
    }

    "respond to health checks in the scenario of total failure" in {
      (healthCheck.healthCheckStatus _).expects().returns(Task.raiseError(new RuntimeException("BOOM!")))
      Get("/health") ~> routes ~> check {
        response.status shouldBe StatusCodes.InternalServerError
      }
    }

//    "respond to aggregations" in {
//      (cache.find _).expects(*, *, *, *).returns(Task.now(None))
//      (cache.update _).expects(*, *, *, *, *).returns(Task(Statistic(1L, 1)))
//
//      Get("/view/aggregations?msamds=34980,23434&actions_taken=5&years=2018") ~> routes ~> check {
//        response.status shouldBe StatusCodes.OK
//        responseAs[AggregationResponse]
//      }
//    }

    "respond to raw Pipe data requests" in {
      (fileStorage.retrieveDataUrl _).expects(*, *, "2018").returns(Task.now(None))
      (fileStorage.persistData _).expects(*, *, "2018", *).returns(Task.unit)

      Get("/view/pipe?msamds=34980,23434&years=2018") ~> routes ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "respond to raw CSV data requests" in {
      (fileStorage.retrieveDataUrl _).expects(*, *, "2018").returns(Task.now(None))
      (fileStorage.persistData _).expects(*, *, "2018", *).returns(Task.unit)

      Get("/view/csv?msamds=34980,23434&years=2018") ~> routes ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "response to count aggregation requests" in {
      (cache.find _).expects(*, *, *, *).returns(Task.now(None))
      (cache.update _).expects(*, *, *, *, *).returns(Task(Statistic(1L, 1)))

      Get("/view/count?years=2018&msamds=34980&states=CA") ~> routes ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

//    "respond to filer requests" in {
//      (cache.findFilers2018 _).expects(*, *).returns(Task.now(None))
//      (cache.updateFilers2018 _).expects(*, *, *).returns(Task.now(FilerInstitutionResponseLatest(Nil)))
//
//      Get("/view/filers?years=2018") ~> routes ~> check {
//        response.status shouldBe StatusCodes.OK
//      }
//    }

    "respond to failed filer requests due to a cache error" in {
      (cache.findFilers2018 _).expects(*, *).returns(Task.raiseError(new RuntimeException("BOOM")))

      Get("/view/filers?years=2018") ~> routes ~> check {
        response.status shouldBe StatusCodes.InternalServerError
      }
    }

    "respond to nationwide aggregation queries" in {
      (cache.find _).expects(*, *, *, *).returns(Task.now(None))
      (cache.update _).expects(*, *, *, *, *).returns(Task(Statistic(1L, 1)))

      Get(
        "/view/nationwide/aggregations?years=2018&actions_taken=4"
      ) ~> routes ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "respond to nationwide raw pipe queries" in {
      (fileStorage.retrieveDataUrl _).expects(*, *, "2018").returns(Task.now(None))
      (fileStorage.persistData _).expects(*, *, "2018", *).returns(Task.unit)

      Get(
        "/view/nationwide/pipe?years=2018&actions_taken=1,4"
      ) ~> routes ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "respond to nationwide raw csv queries" in {
      (fileStorage.retrieveDataUrl _).expects(*, *, "2018").returns(Task.now(None))
      (fileStorage.persistData _).expects(*, *, "2018", *).returns(Task.unit)

      Get(
        "/view/nationwide/csv?years=2018&actions_taken=1,2,3,4,5,6,7,8&msamds=34980"
      ) ~> routes ~> check {}
    }
  }

  override def bootstrapSqlFile: String = "modifiedlar.sql"
}