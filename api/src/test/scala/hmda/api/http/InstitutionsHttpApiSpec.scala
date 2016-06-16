package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.persistence.InstitutionPersistence._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.util.TestData
import hmda.model.fi.Institution
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import scala.concurrent.duration._

class InstitutionsHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with InstitutionsHttpApi with BeforeAndAfterAll {
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec = system.dispatcher

  override def beforeAll(): Unit = {
    createInstitutionsFiling(system)
    TestData.loadData(system)
  }

  "Institutions HTTP API" must {
    "return a list of existing institutions" in {
      Get("/institutions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Set[Institution]] mustBe TestData.institutions
      }
    }
  }

}
