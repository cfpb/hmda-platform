package hmda.api.http.institutions.submissions

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.{ EditResult, _ }
import hmda.model.fi._
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.HmdaFileValidator
import hmda.validation.engine._
import org.scalatest.words.MatcherWords

import scala.concurrent.Future

class SubmissionEditPathsSpec extends InstitutionHttpApiSpec {

  val supervisor = system.actorSelection("/user/supervisor")

  override def beforeAll(): Unit = {
    super.beforeAll()
    loadValidationErrors()
  }

  val s020Description = "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code."
  val s010Description = "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR)."
  val v280Description = "MSA/MD must = a valid Metropolitan Statistical Area or Metropolitan Division (if appropriate) code for period being processed or NA."
  val v285Description = "State must = a valid FIPS code or (NA where MSA/MD = NA)."
  val s020 = EditResult("S020", s020Description, ts = true, List(LarEditResult(LarId("loan1"))))
  val s010 = EditResult("S010", s010Description, ts = false, List(LarEditResult(LarId("loan1"))))
  val v280 = EditResult("V280", v280Description, ts = false, List(LarEditResult(LarId("loan1"))))
  val v285 = EditResult("V285", v285Description, ts = false, List(LarEditResult(LarId("loan2")), LarEditResult(LarId("loan3"))))

  "return summary of validation errors" in {
    val expectedSummary = SummaryEditResults(
      EditResults(List(s020, s010)),
      EditResults(List(v285, v280)),
      EditResults.empty,
      MacroResults(List(MacroResult("Q007", MacroEditJustificationLookup.getJustifications("Q007"))))
    )

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[SummaryEditResults] mustBe expectedSummary
    }
  }

  "Return summary edits in CSV format" in {
    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits?format=csv") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[String] must include("editType, editId, loanId")
      responseAs[String] must include("syntactical, S020, Transmittal Sheet")
      responseAs[String] must include("validity, V285, loan2")
    }
  }

  "return a list of validation errors for a single type" in {
    val expectedEdits = EditResults(List(v285, v280))

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/validity") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[EditResults] mustBe expectedEdits
    }

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/macro") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[MacroResults] mustBe MacroResults(List(MacroResult("Q007", MacroEditJustificationLookup.getJustifications("Q007"))))
    }
  }

  "Return single type edits in CSV format" in {
    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/macro?format=csv") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[String] must include("editType, editId")
      responseAs[String] must include("macro, Q007")
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
    val justification = MacroEditJustificationLookup.getJustifications("Q007").head.copy(verified = true)
    val justifyEdit = MacroEditJustificationWithName("Q007", justification)
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/edits/macro", justifyEdit) ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      val macroResults = responseAs[MacroResults].edits.head
      macroResults.justifications.head.verified mustBe true
      macroResults.justifications.tail.map(x => x.verified mustBe false)
    }
    val justification2 = MacroEditJustificationLookup.getJustifications("Q007").head.copy(verified = false)
    val justifyEdit2 = MacroEditJustificationWithName("Q007", justification2)
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/edits/macro", justifyEdit2) ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      val macroResults = responseAs[MacroResults].edits.head
      macroResults.justifications.head.verified mustBe false
      macroResults.justifications.tail.map(x => x.verified mustBe false)
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
