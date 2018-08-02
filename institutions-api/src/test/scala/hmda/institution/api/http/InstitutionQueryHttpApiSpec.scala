package hmda.institution.api.http

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class InstitutionQueryHttpApiSpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with ScalatestRouteTest
    with InstitutionQueryHttpApi {

  val duration = 5.seconds

  override val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(duration)
  override val log: LoggingAdapter = NoLogging

  override def beforeAll = {
    super.beforeAll()
    Await.result(repository.createSchema(), duration)
  }

  override def afterAll = {
    super.afterAll()
    Await.result(repository.dropSchema(), duration)
  }

  "Institution Query HTTP API" must {
    "search by LEI" in {
      Get("/institutions/XXX") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
  }

}
