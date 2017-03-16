package hmda.api.http.institutions.submissions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.actor.ActorRef
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.{ ErrorResponse, ParsingErrorSummary }
import hmda.model.fi._
import hmda.parser.fi.lar.LarParsingError
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.HmdaFileParser
import hmda.persistence.processing.HmdaFileParser.{ HmdaFileParseState, LarParsedErrors, TsParsedErrors }

import scala.concurrent.Await
import scala.concurrent.duration._

class SubmissionParseErrorsPathsSpec extends InstitutionHttpApiSpec {

  "Submission Parse Errors Path" must {
    "return no errors for an unparsed submission" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val summary = responseAs[ParsingErrorSummary]
        summary.transmittalSheetErrors mustBe Seq.empty
        summary.larErrors mustBe Seq.empty
      }
    }

    "return errors for a parsed submission" in {
      val actor: ActorRef = parserActorFor(SubmissionId("0", "2017", 1))

      val errors = LarParsingError(10, List("test", "ing"))
      actor ! LarParsedErrors(errors)
      currentParseState(actor).larParsingErrors.size mustBe 1

      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val summary = responseAs[ParsingErrorSummary]
        summary.transmittalSheetErrors mustBe Seq.empty
        summary.larErrors mustBe List(LarParsingError(10, List("test", "ing")))
      }
    }

    ////// Pagination /////

    "Set up: persist 2 TS errors and 42 LAR errors" in {
      val actor: ActorRef = parserActorFor(SubmissionId("0", "2017", 2))

      val tsErrors = List("TS 1", "TS 2")
      actor ! TsParsedErrors(tsErrors)

      1.to(42).foreach { i =>
        val err = LarParsingError(i, List(s"$i"))
        actor ! LarParsedErrors(err)
      }

      currentParseState(actor).larParsingErrors.size mustBe 42
    }

    "return first page (up to 20 errors) if request doesn't include 'page' query param" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/2/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val summary = responseAs[ParsingErrorSummary]
        summary.transmittalSheetErrors mustBe List("TS 1", "TS 2")
        summary.larErrors.size mustBe 19
        summary.larErrors.head.lineNumber mustBe 1
      }
    }

    "return next 20 errors on page 2" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/2/parseErrors?page=2") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val summary = responseAs[ParsingErrorSummary]
        summary.transmittalSheetErrors mustBe List()
        summary.larErrors.size mustBe 20
        summary.larErrors.head.lineNumber mustBe 20
      }
    }

    "return last 3 errors on page 3" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/2/parseErrors?page=3") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val summary = responseAs[ParsingErrorSummary]
        summary.transmittalSheetErrors mustBe List()
        summary.larErrors.size mustBe 3
        summary.larErrors.head.lineNumber mustBe 40
      }
    }

    "include pagination metadata" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/2/parseErrors?page=3") ~> institutionsRoutes ~> check {
        val summary = responseAs[ParsingErrorSummary]
        summary.path mustBe "/institutions/0/filings/2017/submissions/2/parseErrors"
        summary.currentPage mustBe 3
        summary.total mustBe 43
      }
    }

    ////// "Not Found" Responses /////

    "Return 404 for nonexistent institution" in {
      getWithCfpbHeaders("/institutions/xxxxx/filings/2017/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "Institution xxxxx not found"
      }
      // Return same error if other url parameters are also wrong
      getWithCfpbHeaders("/institutions/xxxxx/filings/1980/submissions/0/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "Institution xxxxx not found"
      }
    }
    "Return 404 for nonexistent filing period" in {
      getWithCfpbHeaders("/institutions/0/filings/1980/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "1980 filing period not found for institution 0"
      }
    }
    "Return 404 for nonexistent submission" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/0/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "Submission 0 not found for 2017 filing period"
      }
    }
  }

  def parserActorFor(subId: SubmissionId): ActorRef = {
    val fActor = (supervisor ? FindProcessingActor(HmdaFileParser.name, subId)).mapTo[ActorRef]
    Await.result(fActor, 5.seconds)
  }

  def currentParseState(actor: ActorRef): HmdaFileParseState = {
    val state = (actor ? GetState).mapTo[HmdaFileParseState]
    Await.result(state, 5.seconds)
  }
}
