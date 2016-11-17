package hmda.api.http.institutions

import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Path
import hmda.api.http.InstitutionHttpApiSpec
import hmda.model.fi._
import hmda.persistence.HmdaSupervisor.FindSubmissions
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.{ GetSubmissionById, UpdateSubmissionStatus }
import akka.pattern.ask
import hmda.api.protocol.processing.SubmissionProtocol
import spray.json.JsValue

import scala.concurrent.Await

class UploadPathsSpec extends InstitutionHttpApiSpec with SubmissionProtocol with UploadPaths {
  val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"
  val file = multiPartFile(csv, "sample.txt")

  val badContent = "qdemd"
  val badFile = multiPartFile(badContent, "sample.dat")

  val id = SubmissionId("0", "2017", 1)

  "Upload Paths" must {

    "return proper response when uploading a HMDA file" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Accepted
        val submission = responseAs[Submission]
        submission.status mustBe Uploaded
        submission.start must be < System.currentTimeMillis()
      }
    }

    "return 400 when trying to upload the wrong file" in {
      val path = Path("/institutions/0/filings/2017/submissions/1")
      postWithCfpbHeaders(path.toString, badFile) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[Submission] mustBe Submission(id, Failed("Invalid File Format"), 0L, 0L)
      }
    }

    "return a 400 when trying to upload to a non-existant submission" in {
      val path = "/institutions/0/filings/2017/submissions/987654321"
      postWithCfpbHeaders(path, file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[Submission] mustBe Submission(SubmissionId("0", "2017", 987654321), Failed("Submission 987654321 not available for upload"), 0L, 0L)
      }
    }

    "return 400 when trying to upload to a completed submission" in {
      val supervisor = system.actorSelection("/user/supervisor")
      val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, "0", "2017")).mapTo[ActorRef]
      val submissionsActor = Await.result(fSubmissionsActor, 5.seconds)
      submissionsActor ! UpdateSubmissionStatus(SubmissionId("0", "2017", 1), Signed)
      val submission = Await.result((submissionsActor ? GetSubmissionById(SubmissionId("0", "2017", 1))).mapTo[Submission], 5.seconds)

      submission.status mustBe Signed

      val file = multiPartFile("bad file content", "sample.txt")
      val path = Path("/institutions/0/filings/2017/submissions/1")
      postWithCfpbHeaders(path.toString, file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[Submission] mustBe Submission(id, Failed("Submission 1 not available for upload"), 0L, 0L)
      }
    }
  }

}
