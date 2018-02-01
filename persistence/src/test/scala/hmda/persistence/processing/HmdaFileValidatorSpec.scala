package hmda.persistence.processing

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.validation._
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.HmdaSupervisor
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.processing.HmdaFileValidatorState.{HmdaVerificationState, QMState, SVState}
import hmda.persistence.messages.events.processing.HmdaFileParserEvents.{LarParsed, TsParsed}
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.ProcessingMessages.{BeginValidation, Persisted, ValidationCompletedWithErrors}
import hmda.persistence.processing.SingleLarValidation._
import hmda.validation.engine._
import org.scalatest.BeforeAndAfterEach
import hmda.validation.stats.ValidationStats._

class HmdaFileValidatorSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {

  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId1 = SubmissionId("0", "2017", 1)
  val submissionId2 = SubmissionId("0", "2017", 2)

  val larValidator = system.actorSelection(createSingleLarValidator(system).path)
  val hmdaFileParser = createHmdaFileParser(system, submissionId2)
  val validationStats = createValidationStats(system)
  val submissionManager = system.actorOf(SubmissionManager.props(validationStats, submissionId1))
  val supervisor = system.actorOf(HmdaSupervisor.props(validationStats))

  var hmdaFileValidator: ActorRef = _
  val probe = TestProbe()

  val lines = fiCSVEditErrors.split("\n")

  override def beforeEach(): Unit = {
    hmdaFileValidator = createHmdaFileValidator(system, supervisor, validationStats, submissionId1)
  }

  override def afterAll(): Unit = {
    hmdaFileValidator ! Shutdown
    system.terminate()
  }

  val e1 = SyntacticalValidationError("1", "S999", false)
  val e2 = ValidityValidationError("1", "V999", true)
  val e3 = QualityValidationError("1", "Q999", false)
  val e4 = MacroValidationError("Q007")
  val e2duplicate = ValidityValidationError("2", "V999", true)

  val ts = TsCsvParser(lines(0)).right.get
  val lars = lines.tail.map(line => LarCsvParser(line).right.get)

  "HMDA File Validator" must {

    "persist LARs and TS" in {
      probe.send(hmdaFileValidator, ts)
      lars.foreach(lar => probe.send(hmdaFileValidator, lar))
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(HmdaVerificationState(ts = Some(ts), larCount = lars.size))
    }

    "persist validation errors" in {
      val larErrors = LarValidationErrors(Seq(e1, e2, e3, e4, e2duplicate))
      val tsErrors = TsValidationErrors(Seq(e1, e2, e3))
      probe.send(hmdaFileValidator, larErrors)
      probe.send(hmdaFileValidator, tsErrors)

      probe.send(hmdaFileValidator, GetSVState)
      probe.expectMsg(SVState(Set("S999"), Set("V999")))
      probe.send(hmdaFileValidator, GetQMState)
      probe.expectMsg(QMState(Set("Q999"), Set("Q007")))
    }

    "read parsed data and validate it" in {
      probe.send(hmdaFileParser, TsParsed(ts))
      lars.foreach { lar =>
        probe.send(hmdaFileParser, LarParsed(lar))
        probe.expectMsg(Persisted)
      }
      val hmdaFileValidator2 = createHmdaFileValidator(system, supervisor, validationStats, submissionId2)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(5, Nil))

      probe.send(hmdaFileValidator2, BeginValidation(probe.testActor))
      probe.expectMsg(ValidationStarted(submissionId2))
      probe.expectMsgType[ValidationCompletedWithErrors]

      probe.send(hmdaFileValidator2, GetState)
      probe.expectMsg(HmdaVerificationState(false, false, Some(ts), 4))
      probe.send(hmdaFileValidator, GetSVState)
      probe.expectMsg(SVState(Set("S999"), Set("V999")))
      probe.send(hmdaFileValidator, GetQMState)
      probe.expectMsg(QMState(Set("Q999"), Set("Q007")))
    }

    "verify quality edits" in {
      // establish baseline
      probe.send(hmdaFileValidator, GetState)
      val originalState = HmdaVerificationState(false, false, Some(ts), 4)
      probe.expectMsg(originalState)

      // send VerifyQualityEdits message
      probe.send(hmdaFileValidator, VerifyEdits(Quality, true, submissionManager))

      // expect updated validation state
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(originalState.copy(qualityVerified = true))
    }

    "verify macro edits" in {
      // establish baseline
      probe.send(hmdaFileValidator, GetState)
      val originalState = HmdaVerificationState(true, false, Some(ts), 4)
      probe.expectMsg(originalState)

      // send VerifyMacroEdits message
      probe.send(hmdaFileValidator, VerifyEdits(Macro, true, submissionManager))

      // expect updated validation state
      probe.send(hmdaFileValidator, GetState)
      probe.expectMsg(originalState.copy(macroVerified = true))
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
