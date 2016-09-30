package hmda.api.http.institutions

import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.ErrorResponse
import hmda.model.fi.{ Signed, Submission, SubmissionId }
import hmda.persistence.HmdaSupervisor.FindSubmissions
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.{ GetSubmissionById, UpdateSubmissionStatus }
import akka.pattern.ask

import scala.concurrent.Await

class UploadPathsSpec extends InstitutionHttpApiSpec with UploadPaths {

  "Upload Paths" must {

    "return proper response when uploading a HMDA file" in {
      val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"

      val file = multiPartFile(csv, "sample.txt")

      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[String] mustBe "uploaded"
      }
    }

    "return 400 when trying to upload the wrong file" in {
      val badContent = "qdemd"
      val file = multiPartFile(badContent, "sample.dat")
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[ErrorResponse] mustBe ErrorResponse(400, "Invalid File Format", "institutions/0/filings/2017/submissions/1")
      }
    }

    "return 400 when trying to upload to a completed submission" in {
      val badContent = "qdemd"
      val file = multiPartFile(badContent, "sample.txt")
      val supervisor = system.actorSelection("/user/supervisor")
      val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, "0", "2017")).mapTo[ActorRef]

      for {
        s <- fSubmissionsActor
      } yield {
        s ! UpdateSubmissionStatus(SubmissionId("0", "2017", 1), Signed)

        val submission = Await.result((s ? GetSubmissionById(SubmissionId("0", "2017", 1))).mapTo[Submission], 5.seconds)
        submission.submissionStatus mustBe Signed

        postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
          status mustBe StatusCodes.BadRequest
          responseAs[ErrorResponse] mustBe ErrorResponse(400, "Submission already exists", "institutions/0/filings/2017/submissions/1")
        }
      }
    }
  }

}
