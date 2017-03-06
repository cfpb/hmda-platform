package hmda.api.http.institutions.submissions

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.{EditResult, _}
import hmda.model.fi._
import hmda.model.fi.lar.{LarGenerators, LoanApplicationRegister}
import hmda.model.fi.ts.{TransmittalSheet, TsGenerators}
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.processing.HmdaFileValidator
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.engine._
import spray.json.{JsNumber, JsObject, JsString}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class SubmissionEditPathsSpec extends InstitutionHttpApiSpec with LarGenerators with TsGenerators {

  val supervisor = system.actorSelection("/user/supervisor")

  override def beforeAll(): Unit = {
    super.beforeAll()
    loadValidationErrors()
  }

  val s020Description = "Agency code must = 1, 2, 3, 5, 7, 9. The agency that submits the data must be the same as the reported agency code."
  val s010Description = "The first record identifier in the file must = 1 (TS). The second and all subsequent record identifiers must = 2 (LAR)."
  val v280Description = "MSA/MD must = a valid Metropolitan Statistical Area or Metropolitan Division (if appropriate) code for period being processed or NA."
  val v285Description = "State must = a valid FIPS code or (NA where MSA/MD = NA)."
  val s010FieldsL1 = JsObject(("Record Identifier", JsNumber(111)))
  val s020FieldsL1 = JsObject(("Agency Code", JsNumber(222)))
  val v280FieldsL1 = JsObject(("Metropolitan Statistical Area / Metropolitan Division", JsString("333")))
  val v285FieldsL2 = JsObject(("State Code", JsString("444")), ("Metropolitan Statistical Area / Metropolitan Division", JsString("555")))
  val v285FieldsL3 = JsObject(("State Code", JsString("666")), ("Metropolitan Statistical Area / Metropolitan Division", JsString("777")))
  val s020FieldsTs = JsObject(("Agency Code", JsNumber(888)))

  val s020 = EditResult("S020", s020Description, List(EditResultRow(RowId("Transmittal Sheet"), s020FieldsTs), EditResultRow(RowId("loan1"), s020FieldsL1)))
  val s010 = EditResult("S010", s010Description, List(EditResultRow(RowId("loan1"), s010FieldsL1)))
  val v280 = EditResult("V280", v280Description, List(EditResultRow(RowId("loan1"), v280FieldsL1)))
  val v285 = EditResult("V285", v285Description, List(EditResultRow(RowId("loan2"), v285FieldsL2), EditResultRow(RowId("loan3"), v285FieldsL3)))

  "return summary of validation errors" in {
    val expectedSummary = SummaryEditResults(
      EditResults(List(s020, s010)),
      EditResults(List(v285, v280)),
      QualityEditResults(false, Seq()),
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

  "Sort edits by row with sortBy parameter" in {
    val tsS020 = RowResult("Transmittal Sheet", Seq(RowEditDetail("S020", s020Description, s020FieldsTs)))
    val loan1Result =
      RowResult("loan1", Seq(
        RowEditDetail("S010", s010Description, s010FieldsL1),
        RowEditDetail("S020", s020Description, s020FieldsL1),
        RowEditDetail("V280", v280Description, v280FieldsL1)
      ))
    val loan2Result = RowResult("loan2", Seq(RowEditDetail("V285", v285Description, v285FieldsL2)))
    val loan3Result = RowResult("loan3", Seq(RowEditDetail("V285", v285Description, v285FieldsL3)))

    val expectedMacros =
      MacroResults(List(MacroResult("Q007", MacroEditJustificationLookup.getJustifications("Q007"))))

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits?sortBy=row") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      val rowResponse = responseAs[RowResults]
      rowResponse.rows.head mustBe tsS020
      rowResponse.rows.find(_.rowId == "loan1").get mustBe loan1Result
      rowResponse.rows.find(_.rowId == "loan2").get mustBe loan2Result
      rowResponse.rows.find(_.rowId == "loan3").get mustBe loan3Result
      rowResponse.`macro` mustBe expectedMacros
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

  "Sort single type of edits by row with sortBy parameter (syntactical)" in {
    val expectedRows =
      Seq(
        RowResult("Transmittal Sheet", Seq(RowEditDetail("S020", s020Description, s020FieldsTs))),
        RowResult("loan1", Seq(
          RowEditDetail("S010", s010Description, s010FieldsL1),
          RowEditDetail("S020", s020Description, s020FieldsL1)
        ))
      )

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/syntactical?sortBy=row") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      val rowResponse = responseAs[RowResults]
      rowResponse.rows.toSet mustBe expectedRows.toSet
      rowResponse.`macro` mustBe MacroResults(List())
    }
  }
  "Sort single type of edits by row with sortBy parameter (validity)" in {
    val expectedRows =
      Seq(
        RowResult("loan1", Seq(RowEditDetail("V280", v280Description, v280FieldsL1))),
        RowResult("loan2", Seq(RowEditDetail("V285", v285Description, v285FieldsL2))),
        RowResult("loan3", Seq(RowEditDetail("V285", v285Description, v285FieldsL3)))
      )

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/validity?sortBy=row") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      val rowResponse = responseAs[RowResults]
      rowResponse.rows.toSet mustBe expectedRows.toSet
      rowResponse.`macro` mustBe MacroResults(List())
    }
  }
  "SortBy parameter doesn't affect macro edits" in {
    val expectedMacros =
      MacroResults(List(MacroResult("Q007", MacroEditJustificationLookup.getJustifications("Q007"))))

    getWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits/macro?sortBy=row") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      val rowResponse = responseAs[RowResults]
      rowResponse.rows.toSet mustBe Set()
      rowResponse.`macro` mustBe expectedMacros
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
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/0/edits/syntactical") ~> Route.seal(institutionsRoutes) ~> check {
      status mustBe StatusCodes.MethodNotAllowed
    }
  }

  "Edit Type endpoint: return 405 when posting justification to validity endpoint" in {
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/0/edits/validity") ~> Route.seal(institutionsRoutes) ~> check {
      status mustBe StatusCodes.MethodNotAllowed
    }
  }

  "Verify Quality edits endpoint: Responds with correct json and updates validation state" in {
    val verification = QualityEditsVerification(true)
    val currentStatus = Created

    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/edits/quality", verification) ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK

      // test that it responds correctly
      responseAs[QualityEditsVerifiedResponse] mustBe QualityEditsVerifiedResponse(true, currentStatus)

      // test that it updates validation state
      val state: HmdaFileValidationState = Await.result(fValidationState, 5.seconds)
      state.qualityVerified mustBe true
    }
  }

  private def loadValidationErrors(): Unit = {
    val s1 = SyntacticalValidationError("loan1", "S010", false)
    val s2 = SyntacticalValidationError("loan1", "S020", false)
    val v1 = ValidityValidationError("loan1", "V280", false)
    val v2 = ValidityValidationError("loan2", "V285", false)
    val v3 = ValidityValidationError("loan3", "V285", false)
    val m1 = MacroValidationError("Q007", Nil)

    val l1 = larGen.sample.getOrElse(LoanApplicationRegister.empty)
    val lar1 = l1.copy(
      loan = l1.loan.copy(id = "loan1"),
      id = 111,
      agencyCode = 222,
      geography = l1.geography.copy(msa = "333")
    )

    val l2 = larGen.sample.getOrElse(LoanApplicationRegister.empty)
    val lar2 = l2.copy(
      loan = l2.loan.copy(id = "loan2"),
      geography = l2.geography.copy(state = "444", msa = "555")
    )

    val l3 = larGen.sample.getOrElse(LoanApplicationRegister.empty)
    val lar3 = l3.copy(
      loan = l3.loan.copy(id = "loan3"),
      geography = l3.geography.copy(state = "666", msa = "777")
    )

    val ts = tsGen.sample.getOrElse(TransmittalSheet.empty).copy(agencyCode = 888)

    val larValidationErrors = LarValidationErrors(Seq(s1, s2, v1, v2, v3, m1))
    val tsValidationErrors = TsValidationErrors(Seq(s2.copy(ts = true)))

    for {
      h <- fHmdaValidatorActor
    } yield {
      h ! larValidationErrors
      h ! tsValidationErrors
      h ! lar1
      h ! lar2
      h ! lar3
      h ! ts
    }

  }

  private def fValidationState: Future[HmdaFileValidationState] = {
    for {
      s <- fHmdaValidatorActor
      xs <- (s ? GetState).mapTo[HmdaFileValidationState]
    } yield xs
  }

  private def fHmdaValidatorActor: Future[ActorRef] = {
    val id = "0"
    val period = "2017"
    val seqNr = 1
    val submissionId = SubmissionId(id, period, seqNr)
    (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
  }
}
