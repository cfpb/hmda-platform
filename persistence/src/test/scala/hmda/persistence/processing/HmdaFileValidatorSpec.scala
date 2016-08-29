package hmda.persistence.processing

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.SingleLarValidation._
import hmda.persistence.processing.HmdaFileValidator._
import org.scalatest.BeforeAndAfterEach

class HmdaFileValidatorSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._
  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  val larValidator = system.actorSelection(createSingleLarValidator(system).path)

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  val hmdaFileValidator = createHmdaFileValidator(system, submissionId, larValidator)

  val probe = TestProbe()

  val lines = fiCSV.split("\n")

  override def beforeEach(): Unit = {
    parseLars(hmdaFileParser, probe, lines)
  }

  "A clean HMDA File" must {
    "be validated and persisted without errors" in {
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState())
      probe.send(hmdaFileValidator, BeginValidation)
      Thread.sleep(300)
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(3, Nil, Nil, Nil))
    }
  }

}
