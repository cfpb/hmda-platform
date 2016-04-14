package hmda.api.processing.lar

import java.io.File
import hmda.api.processing.ActorSpec
import hmda.api.processing.lar.SingleLarValidation.CheckLar
import hmda.parser.fi.lar.LarCsvParser
import scala.io.Source

class SingleLarValidationSpec extends ActorSpec {

  val larValidation = system.actorOf(SingleLarValidation.props, "larValidation")

  val lines = Source.fromFile(new File("parser/src/test/resources/txt/FirstTestBankData_clean_407_2017.txt")).getLines()
  val lars = lines.drop(1).map(l => LarCsvParser(l))

  "LAR Validation" must {
    "validate all lars in sample files" in {
      lars.foreach { lar =>
        larValidation ! CheckLar(lar)
        expectMsg(Nil)
      }
    }
  }

}
