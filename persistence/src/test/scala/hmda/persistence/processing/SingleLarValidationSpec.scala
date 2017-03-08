package hmda.persistence.processing

import java.io.File

import akka.testkit.TestProbe
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.SingleLarValidation._
import hmda.validation.context.ValidationContext
import hmda.validation.engine.LarValidationErrors
import scala.concurrent.duration._
import scala.io.Source

class SingleLarValidationSpec extends ActorSpec {

  val probe = TestProbe()

  val larValidation = createSingleLarValidator(system)

  val lines = Source.fromFile(new File("parser/jvm/src/test/resources/txt/edits-SVQM_412-lars.txt")).getLines()
  val lars = lines.drop(1).map(line => LarCsvParser(line)).collect {
    case Right(lar) => lar
  }

  "LAR Validation" must {
    "validate all lars in sample files" in {
      lars.foreach { lar =>
        probe.send(larValidation, CheckSyntactical(lar, ValidationContext(None, None)))
        probe.expectMsg(10.seconds, LarValidationErrors(Nil))
      }
      lars.foreach { lar =>
        probe.send(larValidation, CheckValidity(lar, ValidationContext(None, None)))
        probe.expectMsg(10.seconds, LarValidationErrors(Nil))
      }
    }
  }

}
