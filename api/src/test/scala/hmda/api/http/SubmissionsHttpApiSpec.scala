package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.demo.DemoData
import hmda.api.model.Submissions
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.duration._
import hmda.api.persistence.SubmissionPersistence._

class SubmissionsHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with SubmissionsHttpApi with BeforeAndAfterAll {
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  override def beforeAll(): Unit = {
    createSubmissions("12345", "2017", system)
    DemoData.loadData(system)
  }

  "Submissions HTTP API" must {
    "return a list of submissions for a financial institution, for a given filing" in {
      Get("/institutions/12345/filings/2017/submissions") ~> submissionRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Submissions] mustBe Submissions(DemoData.newSubmissions.reverse)
      }
    }
  }

}
