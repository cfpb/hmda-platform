package hmda.api.http.institutions.submissions

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.MethodRejection
import akka.pattern.ask
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model._
import hmda.model.fi._
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.demo.DemoData
import hmda.persistence.processing.HmdaFileValidator
import hmda.validation.engine._

import scala.concurrent.Future

class SubmissionEditPathsSpec extends InstitutionHttpApiSpec {

  val supervisor = system.actorSelection("/user/supervisor")

  override def beforeAll(): Unit = {
    super.beforeAll()
    loadValidationErrors()
  }

  "return summary of validation errors" in {
    val expectedSummary = SummaryEditResults(
      EditResults(
        List(
          EditResult("S020", List(LarEditResult(LarId("loan1")))),
          EditResult("S010", List(LarEditResult(LarId("loan1"))))
        )
      ),
      EditResults(
        List(
          EditResult("V285", List(LarEditResult(LarId("loan2")), LarEditResult(LarId("loan3")))),
          EditResult("V280", List(LarEditResult(LarId("loan1"))))
        )
      ),
      EditResults.empty,
      EditResults.empty
    )

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[SummaryEditResults] mustBe expectedSummary
    }
  }

  "return a list of validation errors for a single type" in {
    val expectedEdits =
      EditResults(
        List(
          EditResult("V285", List(LarEditResult(LarId("loan2")), LarEditResult(LarId("loan3")))),
          EditResult("V280", List(LarEditResult(LarId("loan1"))))
        )
      )

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/validity") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[EditResults] mustBe expectedEdits
    }
  }

  private def loadValidationErrors(): Unit = {
    val supervisor = system.actorSelection("/user/supervisor")
    val id = "0"
    val period = "2017"
    val seqNr = 1
    val submissionId = SubmissionId(id, period, seqNr)
    val fHmdaValidator = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]

    val s1 = ValidationError("loan1", "S010", Syntactical)
    val s2 = ValidationError("loan1", "S020", Syntactical)
    val v1 = ValidationError("loan1", "V280", Validity)
    val v2 = ValidationError("loan2", "V285", Validity)
    val v3 = ValidationError("loan3", "V285", Validity)
    val validationErrors = LarValidationErrors(Seq(s1, s2, v1, v2, v3))

    val fValidate: Future[Unit] = for {
      h <- fHmdaValidator
    } yield {
      h ! validationErrors
    }

  }
}
