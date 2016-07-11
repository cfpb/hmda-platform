package hmda.api.processing.lar

import java.io.File

import akka.testkit.TestProbe
import hmda.actor.test.ActorSpec
import hmda.parser.fi.lar.LarCsvParser

import scala.io.Source
import hmda.api.processing.lar.SingleLarValidation._

import scala.concurrent.duration._

class SingleLarValidationSpec extends ActorSpec {

  val probe = TestProbe()

  val larValidation = createSingleLarValidator(system)

  val lines = Source.fromFile(new File("parser/src/test/resources/txt/FirstTestBankData_clean_407_2017.txt")).getLines()
  val lars = lines.drop(1).map(l => LarCsvParser(l)).filter(_.isRight).map(_.right.get)

  "LAR Validation" must {
    "validate all lars in sample files" in {
      lars.foreach { lar =>
        probe.send(larValidation, CheckSyntactical(lar))
        probe.expectMsg(10.seconds, Nil)
      }
      lars.foreach { lar =>
        probe.send(larValidation, CheckValidity(lar))
        probe.expectMsg(10.seconds, Nil)
      }
    }
  }

}
