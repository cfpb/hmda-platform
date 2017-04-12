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
import hmda.validation.ValidationStats._

class HmdaFileValidatorSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._
  val config = ConfigFactory.load()

  val submissionId1 = SubmissionId("0", "2017", 1)
  val submissionId2 = SubmissionId("0", "2017", 2)

  val larValidator = system.actorSelection(createSingleLarValidator(system).path)

  val hmdaFileParser = createHmdaFileParser(system, submissionId2)

  val validationStats = createValidationStats(system)

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
  val e4 = MacroValidationError("Q007")

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
        Seq(e4),
        macroVerified = false
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
        List(
          SyntacticalValidationError("38800009923", "S025", true)
        ),
        Nil,
        Nil,
        List(
          SyntacticalValidationError("8299422144", "S020", false),
          SyntacticalValidationError("8299422144", "S025", false),
          SyntacticalValidationError("9471480396", "S025", false),
          SyntacticalValidationError("2185751599", "S010", false),
          SyntacticalValidationError("2185751599", "S020", false),
          SyntacticalValidationError("2185751599", "S025", false),
          SyntacticalValidationError("4977566612", "S025", false)
        ),
        List(
          ValidityValidationError("4977566612", "V550", false),
          ValidityValidationError("4977566612", "V555", false),
          ValidityValidationError("4977566612", "V560", false)
        ),
        Nil,
        qualityVerified = false,
        List(
          MacroValidationError("Q008"),
          MacroValidationError("Q010"),
          MacroValidationError("Q016"),
          MacroValidationError("Q023")
        ),
        macroVerified = false
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
        Vector(e4),
        macroVerified = false
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
        Vector(e4),
        macroVerified = false
      ))
    }

    "verify macro edits" in {
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
        qualityVerified = true,
        Vector(e4),
        macroVerified = false
      ))

      // send VerifyQualityEdits message
      probe.send(hmdaFileValidator, VerifyEdits(Macro, true))
      probe.expectMsg(EditsVerified(Macro, true))

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
        Vector(e4),
        macroVerified = true
      ))
    }

    val tsS987 = SyntacticalValidationError("tsheet", "S987", true)
    val larS987 = SyntacticalValidationError("lar", "S987", false)
    val lars42: Seq[SyntacticalValidationError] = Seq.fill(42)(larS987)

    "get paginated results for a single named edit" in {
      //Setup: persist enough failures for S987 that pagination is necessary
      val tsErrors = TsValidationErrors(Seq(tsS987))
      val larErrors = LarValidationErrors(lars42)
      probe.send(hmdaFileValidator, tsErrors)
      probe.send(hmdaFileValidator, larErrors)

      //First page should have errors 1 - 20
      probe.send(hmdaFileValidator, GetNamedErrorResultsPaginated("S987", 1))
      val pg1 = probe.expectMsgType[PaginatedErrors]
      pg1.errors.size mustBe 20
      pg1.errors.head mustBe tsS987

      //Second page should have errors 21 - 40
      probe.send(hmdaFileValidator, GetNamedErrorResultsPaginated("S987", 2))
      val pg2 = probe.expectMsgType[PaginatedErrors]
      pg2.errors.size mustBe 20

      //Third page should have remaining errors (41 - 43)
      probe.send(hmdaFileValidator, GetNamedErrorResultsPaginated("S987", 3))
      val pg3 = probe.expectMsgType[PaginatedErrors]
      pg3.errors.size mustBe 3
    }

  }

}
