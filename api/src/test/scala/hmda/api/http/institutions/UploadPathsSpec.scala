package hmda.api.http.institutions

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Path
import akka.pattern.ask
import hmda.api.http.InstitutionHttpApiAsyncSpec
import hmda.api.protocol.processing.SubmissionProtocol
import hmda.model.fi._
import hmda.persistence.HmdaSupervisor.FindSubmissions
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.UpdateSubmissionStatus
import hmda.query.DbConfiguration._
import hmda.query.model.filing.ModifiedLoanApplicationRegister
import scala.concurrent.{ Await, Future }

class UploadPathsSpec extends InstitutionHttpApiAsyncSpec with SubmissionProtocol with UploadPaths {
  import config.profile.api._

  val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
    "2|0123456789|9|ABC|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|DEF|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|GHI|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"
  val csv2 = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
    "2|0123456789|8|JKL|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|8|MNO|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|8|PQR|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"
  val file = multiPartFile(csv, "parse-length_4-lars.txt")
  val file2 = multiPartFile(csv2, "parse-length_5-lars.txt")

  val badContent = "qdemd"
  val badFile = multiPartFile(badContent, "sample.dat")

  val id = SubmissionId("0", "2017", 1)

  val repository = new LarRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)

  private def dropAllObjects: Future[Int] = {
    val db = repository.config.db
    val dropAll = sqlu"""DROP ALL OBJECTS"""
    db.run(dropAll)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    val setup = for {
      _ <- dropAllObjects
      _ <- repository.createSchema()
      c <- modifiedLarRepository.createSchema()
    } yield c
    Await.result(setup, duration)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropAllObjects.onComplete {
      case _ => system.terminate()
    }
  }

  "Upload Paths" must {

    "return proper response when uploading a HMDA file" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Accepted
        val submission = responseAs[Submission]
        submission.status mustBe Uploaded
        submission.start must be < System.currentTimeMillis()
      }
    }

    "delete previous submission when re-uploading another HMDA file" in {
      Thread.sleep(5000)
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/2", file2) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Accepted
        val submission = responseAs[Submission]
        submission.status mustBe Uploaded
        submission.start must be < System.currentTimeMillis()
        Thread.sleep(4000) //Needs to go through validation process
        modifiedLarRepository.findByInstitutionId("0").map {
          case xs: Seq[ModifiedLoanApplicationRegister] =>
            xs.length mustBe 3
            xs.head.agencyCode mustBe 8
        }
      }
    }

    "return 400 when trying to upload the wrong file" in {
      val id2 = SubmissionId("0", "2017", 3)
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/3", badFile) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[Submission] mustBe Submission(id2, Failed("Invalid File Format"), 0L, 0L)
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
      val fSubmission = for {
        a <- (supervisor ? FindSubmissions(SubmissionPersistence.name, "0", "2017")).mapTo[ActorRef]
        s <- (a ? UpdateSubmissionStatus(id, Signed)).mapTo[Option[Submission]]
      } yield s

      fSubmission.map { s =>
        s.getOrElse(Submission()).status mustBe Signed
        val file = multiPartFile("bad file content", "parse-length_4-lars.txt")
        val path = Path("/institutions/0/filings/2017/submissions/1")
        postWithCfpbHeaders(path.toString, file) ~> institutionsRoutes ~> check {
          status mustBe StatusCodes.BadRequest
          responseAs[Submission] mustBe Submission(id, Failed("Submission 1 not available for upload"), 0L, 0L)
        }
      }
    }
  }

}
