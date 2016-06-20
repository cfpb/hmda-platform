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
import hmda.model.fi.{ Created, Submission }

class SubmissionsHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with SubmissionsHttpApi with BeforeAndAfterAll {
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  override def beforeAll(): Unit = {
    createSubmissions("12345", "2017", system)
    DemoData.loadData(system)
  }

  "Submissions HTTP API" must {
    val url = "/institutions/12345/filings/2017/submissions"
    "return a list of submissions for a financial institution, for a given filing" in {
      Get(url) ~> submissionRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Submissions] mustBe Submissions(DemoData.newSubmissions.reverse)
      }
    }
    "create a new submission" in {
      Post(url) ~> submissionRoutes ~> check {
        status mustBe StatusCodes.Created
        responseAs[Submission] mustBe Submission(DemoData.newSubmissions.size + 1, Created)
      }
    }
  }

}
