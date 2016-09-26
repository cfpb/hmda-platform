package hmda.api.http.institutions

import akka.actor.ActorRef
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.ask
import akka.util.Timeout
import hmda.api.RequestHeaderUtils
import hmda.api.http.InstitutionsHttpApi
import hmda.api.model.{ EditResult, EditResults, LarEditResult, SummaryEditResults }
import hmda.api.protocol.processing.EditResultsProtocol
import hmda.model.fi.SubmissionId
import hmda.persistence.HmdaSupervisor
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.HmdaFileValidator
import hmda.validation.engine.{ Syntactical, ValidationError, ValidationErrors, Validity }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.Future
import scala.concurrent.duration._

class SubmissionPathsSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with InstitutionsHttpApi
    with EditResultsProtocol
    with BeforeAndAfterAll
    with RequestHeaderUtils {

  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  implicit val ec = system.dispatcher

  val supervisor = system.actorOf(HmdaSupervisor.props(), "supervisor")

  override def beforeAll(): Unit = {
    loadValidationErrors()
  }

  "return summary of validation errors" in {
    val expectedSummary = SummaryEditResults(
      EditResults(
        List(
          EditResult("S020", List(LarEditResult("loan1"))),
          EditResult("S010", List(LarEditResult("loan1")))
        )
      ),
      EditResults(
        List(
          EditResult("V285", List(LarEditResult("loan2"), LarEditResult("loan3"))),
          EditResult("V280", List(LarEditResult("loan1")))
        )
      ),
      EditResults(List()),
      EditResults(List())
    )

    postWithCfpbHeaders(s"/institutions/0/filings/2017/submissions/1/edits") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      responseAs[SummaryEditResults] mustBe expectedSummary
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
    val validationErrors = ValidationErrors(Seq(s1, s2, v1, v2, v3))

    val fValidate: Future[Unit] = for {
      h <- fHmdaValidator
    } yield {
      h ! validationErrors
    }
  }

}
