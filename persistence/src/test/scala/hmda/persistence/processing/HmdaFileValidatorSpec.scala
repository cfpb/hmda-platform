package hmda.persistence.processing

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.SingleLarValidation._
import hmda.persistence.processing.HmdaFileValidator._
import org.scalatest.BeforeAndAfterEach

class HmdaFileValidatorSpec extends ActorSpec with BeforeAndAfterEach {
  import hmda.model.util.FITestData._
  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  val larValidator = system.actorSelection(createSingleLarValidator(system).path)

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  val hmdaFileValidator = createHmdaFileValidator(system, submissionId, larValidator)

  val probe = TestProbe()

  val lines = fiCSV.split("\n")

  override def beforeEach(): Unit = {
    //parseLars(hmdaFileParser, probe, lines)
  }

  "A HMDA File" must {
    "be validated" in {
      probe.send(hmdaFileValidator, BeginValidation)
      //system.eventStream.subscribe(probe.ref, classOf[ValidationStarted])
      //probe.expectMsg(ValidationStarted(submissionId))
    }
  }

}
