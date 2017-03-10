package hmda.validation.engine.lar.validity

import java.io.File

import hmda.parser.fi.lar.LarCsvParser
import hmda.validation.context.ValidationContext
import org.scalatest.{ MustMatchers, WordSpec }

import scala.io.Source

class LarValidityEngineSpec extends WordSpec with MustMatchers with LarValidityEngine {

  "LAR Validity engine" must {
    "pass validation on valid sample file" in {
      val lines = Source.fromFile(new File("parser/jvm/src/test/resources/txt/edits-SVQM_412-lars.txt")).getLines()
      val lars = lines.drop(1).map(line => LarCsvParser(line)).collect {
        case Right(lar) => lar
      }

      lars.foreach { lar =>
        checkValidity(lar, ValidationContext(None, None)).isSuccess mustBe true
      }
    }
  }

}
