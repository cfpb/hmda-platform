package hmda.api.http.institutions.submissions

import akka.actor.ActorRef
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import hmda.api.http.InstitutionHttpApiSpec
import hmda.model.fi.SubmissionId
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.HmdaFileValidator
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.Await
import hmda.parser.fi.ts.TsCsvParser

class SubmissionSummaryPathsSpec extends InstitutionHttpApiSpec with BeforeAndAfterAll {
  import hmda.model.util.FITestData._

  val institutionId = "0"
  val period = "2017"
  val seqNr = 1
  val submissionId = SubmissionId(institutionId, period, seqNr)

  val lines = fiCSV.split("\n")
  val ts = TsCsvParser(lines(0)).right.get

  override def beforeAll(): Unit = {
    super.beforeAll()
    val supervisor = system.actorSelection("/user/supervisor")
    val validatorF = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
    val validator = Await.result(validatorF, duration)
    validator ! ts
  }

  "Submission Summary Paths" must {
    "return a 200" in {
      getWithCfpbHeaders(s"/institutions/$institutionId/filings/$period/submissions/$seqNr/summary") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
  }
}
