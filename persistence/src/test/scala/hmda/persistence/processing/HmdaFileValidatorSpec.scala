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
import hmda.persistence.processing.ProcessingMessages.{ BeginValidation, ValidationCompletedWithErrors }
import hmda.persistence.processing.SingleLarValidation._
import hmda.validation.engine._
import org.scalatest.BeforeAndAfterEach
import hmda.model.fi.lar.fields.LarTopLevelFields._

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
      val e1 = ValidationError("1", ValidationErrorMetaData("S999", Map(noField -> "")), Syntactical)
      val e2 = ValidationError("1", ValidationErrorMetaData("V999", Map(noField -> "")), Validity)
      val e3 = ValidationError("1", ValidationErrorMetaData("Q999", Map(noField -> "")), Quality)
      val e4 = ValidationError("1", ValidationErrorMetaData("Q007", Map(noField -> "")), Macro)
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
        Seq(e4)
      ))
    }

    "read parsed data and validate it" in {
      probe.send(hmdaFileParser, TsParsed(ts))
      lars.foreach(lar => probe.send(hmdaFileParser, LarParsed(lar)))
      val hmdaFileValidator2 = createHmdaFileValidator(system, submissionId2)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(5, Nil))

      probe.send(hmdaFileValidator2, BeginValidation(probe.testActor))
      probe.expectMsg(ValidationStarted(submissionId2))
      probe.expectMsg(ValidationCompletedWithErrors(submissionId2))

      probe.send(hmdaFileValidator2, GetState)
      probe.expectMsg(HmdaFileValidationState(
        Some(ts),
        List(lars(1)),
        Nil,
        Nil,
        Nil,
        List(ValidationError("8299422144", ValidationErrorMetaData("S020", Map(noField -> "")), Syntactical), ValidationError("2185751599", ValidationErrorMetaData("S010", Map(noField -> "")), Syntactical), ValidationError("2185751599", ValidationErrorMetaData("S020", Map(noField -> "")), Syntactical)),
        List(ValidationError("4977566612", ValidationErrorMetaData("V550", Map(noField -> "")), Validity), ValidationError("4977566612", ValidationErrorMetaData("V555", Map(noField -> "")), Validity), ValidationError("4977566612", ValidationErrorMetaData("V560", Map(noField -> "")), Validity)),
        Nil,
        List(ValidationError("", ValidationErrorMetaData("Q008", Map(noField -> "")), Macro), ValidationError("", ValidationErrorMetaData("Q010", Map(noField -> "")), Macro), ValidationError("", ValidationErrorMetaData("Q016", Map(noField -> "")), Macro), ValidationError("", ValidationErrorMetaData("Q023", Map(noField -> "")), Macro))
      ))

    }

  }

}
