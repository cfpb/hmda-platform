package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.ProcessingMessages.{ BeginValidation, Persisted, ValidationCompletedWithErrors }
import hmda.persistence.processing.SingleLarValidation._
import hmda.validation.engine._
import org.scalatest.BeforeAndAfterEach

class HmdaFileValidatorSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._
  val config = ConfigFactory.load()

  val submissionId1 = SubmissionId("0", "2017", 1)
  val submissionId2 = SubmissionId("0", "2017", 2)

  val larValidator = system.actorSelection(createSingleLarValidator(system).path)

  val hmdaFileParser = createHmdaFileParser(system, submissionId2)

  var hmdaFileValidator: ActorRef = _

  val probe = TestProbe()

  val lines = fiCSVEditErrors.split("\n")

  override def beforeEach(): Unit = {
    hmdaFileValidator = createHmdaFileValidator(system, submissionId1)
  }

  override def afterAll(): Unit = {
    hmdaFileValidator ! Shutdown
    system.terminate()
  }

  val e1 = SyntacticalValidationError("1", "S999", false)
  val e2 = ValidityValidationError("1", "V999", true)
  val e3 = QualityValidationError("1", "Q999", false)
  val e4 = MacroValidationError("Q007", Nil)

  val ts = TsCsvParser(lines(0)).right.get
  val lars = lines.tail.map(line => LarCsvParser(line).right.get)

  "HMDA File Validator" must {

    "persist LARs and TS" in {
      probe.send(hmdaFileValidator, ts)
      lars.foreach(lar => probe.send(hmdaFileValidator, lar))
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(Some(ts), lars.toSeq, Nil, Nil, Nil))
    }

    "persist validation errors" in {
      val larErrors = LarValidationErrors(Seq(e1, e2, e3, e4))
      val tsErrors = TsValidationErrors(Seq(e1, e2, e3))
      probe.send(hmdaFileValidator, larErrors)
      probe.send(hmdaFileValidator, tsErrors)
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        Seq(e1),
        Seq(e2),
        Seq(e3),
        Seq(e1),
        Seq(e2),
        Seq(e3),
        qualityVerified = false,
        Seq(e4)
      ))
    }

    "read parsed data and validate it" in {
      probe.send(hmdaFileParser, TsParsed(ts))
      lars.foreach { lar =>
        probe.send(hmdaFileParser, LarParsed(lar))
        probe.expectMsg(Persisted)
      }
      val hmdaFileValidator2 = createHmdaFileValidator(system, submissionId2)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(5, Nil))

      probe.send(hmdaFileValidator2, BeginValidation(probe.testActor))
      probe.expectMsg(ValidationStarted(submissionId2))
      probe.expectMsg(ValidationCompletedWithErrors(submissionId2))

      probe.send(hmdaFileValidator2, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        Nil,
        Nil,
        Nil,
        List(
          SyntacticalValidationError("8299422144", "S020", false),
          SyntacticalValidationError("2185751599", "S010", false),
          SyntacticalValidationError("2185751599", "S020", false)
        ),
        List(
          ValidityValidationError("4977566612", "V550", false),
          ValidityValidationError("4977566612", "V555", false),
          ValidityValidationError("4977566612", "V560", false)
        ),
        Nil,
        qualityVerified = false,
        List(
          MacroValidationError("Q008", Nil),
          MacroValidationError("Q010", Nil),
          MacroValidationError("Q016", Nil),
          MacroValidationError("Q023", Nil)
        )
      ))
    }

    "verify quality edits" in {
      // establish baseline
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        Seq(e1),
        Seq(e2),
        Seq(e3),
        Seq(e1),
        Seq(e2),
        Seq(e3),
        qualityVerified = false,
        Vector(e4)
      ))

      // send VerifyQualityEdits message
      probe.send(hmdaFileValidator, VerifyEdits(Quality, true))
      probe.expectMsg(EditsVerified(Quality, true))

      // expect updated validation state
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        Seq(e1),
        Seq(e2),
        Seq(e3),
        Seq(e1),
        Seq(e2),
        Seq(e3),
        qualityVerified = true,
        Vector(e4)
      ))
    }

    /*
    "verify quality edits" in {
      // establish baseline
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        Seq(e1),
        Seq(e2),
        Seq(e3),
        Seq(e1),
        Seq(e2),
        Seq(e3),
        qualityVerified = false,
        Vector(e4)
      ))

      // send VerifyQualityEdits message
      probe.send(hmdaFileValidator, VerifyQualityEdits(true))
      probe.expectMsg(QualityEditsVerified(true))

      // expect updated validation state
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        lars,
        Seq(e1),
        Seq(e2),
        Seq(e3),
        Seq(e1),
        Seq(e2),
        Seq(e3),
        qualityVerified = true,
        Vector(e4)
      ))
    }
    */

  }

}
