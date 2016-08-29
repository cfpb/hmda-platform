package hmda.persistence.processing

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.model.fi.lar._
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.SingleLarValidation._
import hmda.validation.engine.{ Syntactical, ValidationError }
import org.scalatest.BeforeAndAfterEach

class HmdaFileValidatorSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._
  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  val larValidator = system.actorSelection(createSingleLarValidator(system).path)

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  var hmdaFileValidator: ActorRef = _

  val probe = TestProbe()

  val lines = fiCSV.split("\n")

  val invalidLars = fiCSVInvalidLars.split("\n")

  override def beforeEach(): Unit = {
    hmdaFileValidator = createHmdaFileValidator(system, submissionId, larValidator)
  }

  "A HMDA File" must {
    "be validated and persisted, detecting validation errors" in {
      parseLars(hmdaFileParser, probe, invalidLars)
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState())
      probe.send(hmdaFileValidator, BeginValidation)
      Thread.sleep(500)
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(
        HmdaFileValidationState(
          Set(
            LoanApplicationRegister(2, "8800009923", 3, Loan("8299422144", "20170613", 1, 2, 2, 1, 5), 3, 4, 20170719, Geography("NA", "NA", "NA", "NA"), Applicant(2, 2, 3, "", "", "", "", 3, "", "", "", "", 1, 2, "37"), 0, Denial("", "", ""), "NA", 2, 1),
            LoanApplicationRegister(2, "8800009923", 3, Loan("2185751597", "20170328", 1, 1, 2, 1, 25), 3, 3, 20170425, Geography("NA", "45", "067", "9504.00"), Applicant(2, 5, 3, "", "", "", "", 8, "", "", "", "", 2, 5, "34"), 0, Denial("", "", ""), "NA", 2, 5),
            LoanApplicationRegister(2, "8800009923", 3, Loan("4977566612", "20170920", 1, 1, 1, 1, 46), 3, 3, 20171022, Geography("NA", "45", "067", "9505.00"), Applicant(2, 5, 3, "", "", "", "", 8, "", "", "", "", 2, 5, "23"), 0, Denial("", "", ""), "NA", 2, 1)
          ),
          List(ValidationError("9471480396", "S020", Syntactical)), List(), List()
        )
      )
    }
  }

}
