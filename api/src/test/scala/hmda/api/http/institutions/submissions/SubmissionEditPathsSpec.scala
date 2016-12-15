package hmda.api.http.institutions.submissions

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model._
import hmda.model.fi._
import hmda.persistence.HmdaSupervisor.FindProcessingActor
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
          EditResult("S020", ts = true, List(LarEditResult(LarId("loan1")))),
          EditResult("S010", ts = false, List(LarEditResult(LarId("loan1"))))
        )
      ),
      EditResults(
        List(
          EditResult("V285", ts = false, List(LarEditResult(LarId("loan2")), LarEditResult(LarId("loan3")))),
          EditResult("V280", ts = false, List(LarEditResult(LarId("loan1"))))
        )
      ),
      EditResults.empty,
      MacroResults(List(
        MacroResult(
          "Q007",
          MacroEditJustificationLookup.getJustifications("Q007")
        )
      ))
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
          EditResult("V285", ts = false, List(LarEditResult(LarId("loan2")), LarEditResult(LarId("loan3")))),
          EditResult("V280", ts = false, List(LarEditResult(LarId("loan1"))))
        )
      )

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/validity") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[EditResults] mustBe expectedEdits
    }

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/macro") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[MacroResults] mustBe MacroResults(List(MacroResult("Q007", MacroEditJustificationLookup.getJustifications("Q007"))))
    }
  }

  "Edits endpoint: return 404 for nonexistent institution" in {
    getWithCfpbHeaders(s"/institutions/xxxxx/filings/2017/submissions/1/edits") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse].message mustBe "Institution xxxxx not found"
    }
  }
  "Edits endpoint: return 404 for nonexistent filing period" in {
    getWithCfpbHeaders(s"/institutions/0/filings/1980/submissions/1/edits") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse].message mustBe "1980 filing period not found for institution 0"
    }
  }
  "Edits endpoint: return 404 for nonexistent submission" in {
    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/0/edits") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse].message mustBe "Submission 0 not found for 2017 filing period"
    }
  }

  "Edit Type endpoint: return 404 for nonexistent institution" in {
    getWithCfpbHeaders(s"/institutions/xxxxx/filings/2017/submissions/1/edits/validity") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse].message mustBe "Institution xxxxx not found"
    }
  }
  "Edit Type endpoint: return 404 for nonexistent filing period" in {
    getWithCfpbHeaders(s"/institutions/0/filings/1980/submissions/1/edits/quality") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse].message mustBe "1980 filing period not found for institution 0"
    }
  }
  "Edit Type endpoint: return 404 for nonexistent submission" in {
    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/0/edits/syntactical") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse].message mustBe "Submission 0 not found for 2017 filing period"
    }
  }

  "Justify macro edits" in {
    val justification = MacroEditJustification(1, "Other", true, Some("text"))
    val justifyEdit = MacroEditJustificationWithName("Q007", justification)
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/edits/macro", justifyEdit) ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[MacroEditJustification] mustBe justification
    }
  }

  "Edit Type endpoint: return 405 when posting justification to syntactical endpoint" in {
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/0/edits/syntactical") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.MethodNotAllowed
      responseAs[ErrorResponse].message mustBe "Method not allowed"
    }
  }

  "Edit Type endpoint: return 405 when posting justification to validity endpoint" in {
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/0/edits/validity") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.MethodNotAllowed
      responseAs[ErrorResponse].message mustBe "Method not allowed"
    }
  }

  private def loadValidationErrors(): Unit = {
    val supervisor = system.actorSelection("/user/supervisor")
    val id = "0"
    val period = "2017"
    val seqNr = 1
    val submissionId = SubmissionId(id, period, seqNr)
    val fHmdaValidator = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]

    val s1 = SyntacticalValidationError("loan1", "S010")
    val s2 = SyntacticalValidationError("loan1", "S020")
    val v1 = ValidityValidationError("loan1", "V280")
    val v2 = ValidityValidationError("loan2", "V285")
    val v3 = ValidityValidationError("loan3", "V285")
    val m1 = MacroValidationError("Q007", Nil)
    val larValidationErrors = LarValidationErrors(Seq(s1, s2, v1, v2, v3, m1))

    val tsValidationErrors = TsValidationErrors(Seq(s2))

    val fValidate: Future[Unit] = for {
      h <- fHmdaValidator
    } yield {
      h ! larValidationErrors
      h ! tsValidationErrors
    }

  }
}
