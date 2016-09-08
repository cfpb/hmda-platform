package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.SingleLarValidation._
import hmda.validation.engine._
import org.scalatest.BeforeAndAfterEach

class HmdaFileValidatorSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._
  val config = ConfigFactory.load()

  override implicit lazy val system =
    ActorSystem(
      "test-system",
      ConfigFactory.parseString(
        TestConfigOverride.config
      )
    )

  val submissionId = "12345-2017-1"

  val larValidator = system.actorSelection(createSingleLarValidator(system).path)

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  var hmdaFileValidator: ActorRef = _

  val probe = TestProbe()

  val lines = fiCSV.split("\n")

  override def beforeEach(): Unit = {
    hmdaFileValidator = createHmdaFileValidator(system, submissionId)
  }

  override def afterAll(): Unit = {
    hmdaFileValidator ! Shutdown
  }

  val ts = TsCsvParser(lines(0)).right.get
  val lars = lines.tail.map(line => LarCsvParser(line).right.get)
  "HMDA File Validator" must {
    "persist clean LARs" in {

      probe.send(hmdaFileValidator, ts)
      lars.foreach(lar => probe.send(hmdaFileValidator, lar))
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(Some(ts), lars.toSeq, Nil, Nil, Nil))
    }

    "persist syntactical, validity and quality errors" in {
      val e1 = ValidationError("1", "S020", Syntactical)
      val e2 = ValidationError("1", "V120", Validity)
      val e3 = ValidationError("1", "Q003", Quality)
      val errors = ValidationErrors(Seq(e1, e2, e3))
      probe.send(hmdaFileValidator, errors)
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        Seq(e1),
        Seq(e2),
        Seq(e3)
      ))
    }

    "read parsed data and validate it" in {
      probe.send(hmdaFileParser, TsParsed(ts))
      lars.foreach(lar => probe.send(hmdaFileParser, LarParsed(lar)))
      val hmdaFileValidator2 = createHmdaFileValidator(system, submissionId)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(4, Nil))
      probe.send(hmdaFileValidator2, BeginValidation)
      probe.send(hmdaFileValidator2, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        List(ValidationError("1", "S020", Syntactical)),
        List(ValidationError("1", "V120", Validity)),
        List(ValidationError("1", "Q003", Quality))
      ))

    }

  }

}
