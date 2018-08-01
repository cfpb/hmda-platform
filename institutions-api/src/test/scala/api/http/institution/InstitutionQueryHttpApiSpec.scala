package api.http.institution

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.http.institution.InstitutionQueryHttpApi
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class InstitutionQueryHttpApiSpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with ScalatestRouteTest
    with InstitutionQueryHttpApi {

  val duration = 5.seconds

  override implicit val ec: ExecutionContext = system.dispatcher
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

//  "Institution Query HTTP API" must {
//    "search by LEI" in {
//      Get("/institutions/XXX") ~> institutionPublicRoutes ~> check {
//        status mustBe StatusCodes.NotFound
//      }
//    }
//  }

}
