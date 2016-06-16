package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.duration._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.api.demo.DemoData
import hmda.api.model.Filings
import hmda.api.persistence.FilingPersistence._

class FilingHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with FilingsHttpApi with BeforeAndAfterAll {
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec = system.dispatcher

  override def beforeAll(): Unit = {
    createFilings("12345", system)
    DemoData.loadData(system)
  }

  "Filings HTTP API" must {
    "return a list of filings for a financial institution" in {
      Get("/institutions/12345/filings") ~> filingsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Filings] mustBe Filings(DemoData.filings.filter(f => f.fid == "12345").reverse)
      }
    }

  }

}
