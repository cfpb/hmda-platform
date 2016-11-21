package hmda.api.http.institutions.submissions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.ErrorResponse
import hmda.model.fi._
import hmda.parser.fi.lar.{ LarParsingError, ParsingErrorSummary }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.processing.HmdaFileParser
import hmda.persistence.processing.HmdaFileParser.{ HmdaFileParseState, LarParsedErrors }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class SubmissionParseErrorsPathsSpec extends InstitutionHttpApiSpec {

  val supervisor = system.actorSelection("/user/supervisor")

  "Submission Parse Errors Path" must {
    "return no errors for an unparsed submission" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[ParsingErrorSummary] mustBe ParsingErrorSummary(Seq.empty, Seq.empty)
      }
    }

    "return errors for a parsed submission" in {
      val subId = SubmissionId("0", "2017", 1)
      val fActor = (supervisor ? FindProcessingActor(HmdaFileParser.name, subId)).mapTo[ActorRef]
      val actor = Await.result(fActor, 5.seconds)

      val errors = LarParsingError(10, List("test", "ing"))
      actor ! LarParsedErrors(errors)
      val state = (actor ? GetState).mapTo[HmdaFileParseState]
      val result = Await.result(state, 5.seconds)
      result.larParsingErrors.size mustBe 1

      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[ParsingErrorSummary] mustBe ParsingErrorSummary(List(), List(LarParsingError(10, List("test", "ing"))))
      }
    }

    "Return 404 for nonexistent institution" in {
      getWithCfpbHeaders("/institutions/xxxxx/filings/2017/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "Institution xxxxx not found"
      }
    }
    "Return 404 for nonexistent filing period" in {
      getWithCfpbHeaders("/institutions/0/filings/1980/submissions/1/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "1980 filing not found for institution 0"
      }
    }
    "Return 404 for nonexistent submission" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/0/parseErrors") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "Submission 0 not found for 2017 filing"
      }
    }
  }

}
