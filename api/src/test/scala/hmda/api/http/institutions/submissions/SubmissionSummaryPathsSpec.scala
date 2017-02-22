package hmda.api.http.institutions.submissions

import akka.actor.ActorRef
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.institutions.submissions.{ ContactSummary, FileSummary, RespondentSummary, SubmissionSummary }
import hmda.model.fi.SubmissionId
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.{ HmdaFileValidator, HmdaRawFile, SubmissionManager }
import org.scalatest.BeforeAndAfterAll
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.parser.fi.lar.LarCsvParser

import scala.concurrent.Await
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.processing.HmdaRawFile.AddFileName

class SubmissionSummaryPathsSpec extends InstitutionHttpApiSpec with BeforeAndAfterAll {
  import hmda.model.util.FITestData._

  val institutionId = "0"
  val period = "2017"
  val seqNr = 1
  val submissionId = SubmissionId(institutionId, period, seqNr)

  val fileName = "lars.dat"
  val lines = fiCSV.split("\n")
  val ts = TsCsvParser(lines(0)).right.get
  val lars = lines.tail.map(line => LarCsvParser(line).right.get)

  override def beforeAll(): Unit = {
    super.beforeAll()
    val supervisor = system.actorSelection("/user/supervisor")
    val validatorF = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
    val submissionManagerF = (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
    val validator = Await.result(validatorF, duration)
    val submissionManager = Await.result(submissionManagerF, duration)
    validator ! ts
    lars.foreach(lar => validator ! lar)
    submissionManager ! AddFileName(fileName)
  }

  "Submission Summary Paths" must {
    "return a 200" in {
      getWithCfpbHeaders(s"/institutions/$institutionId/filings/$period/submissions/$seqNr/summary") ~> institutionsRoutes ~> check {
        val contactSummary = ContactSummary(ts.contact.name, ts.contact.phone, ts.contact.email)
        val respondentSummary = RespondentSummary(ts.respondent.name, ts.respondent.id, ts.taxId, ts.agencyCode.toString, contactSummary)
        val fileSummary = FileSummary(fileName, period, lars.size)
        val submissionSummary = SubmissionSummary(respondentSummary, fileSummary)

        status mustBe StatusCodes.OK
        responseAs[SubmissionSummary] mustBe submissionSummary
      }
    }
  }
}
