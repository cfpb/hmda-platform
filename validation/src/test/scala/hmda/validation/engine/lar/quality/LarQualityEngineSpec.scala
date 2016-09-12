package hmda.validation.engine.lar.quality

import java.io.File

import hmda.parser.fi.lar.{ LarCsvParser, LarGenerators }
import hmda.validation.context.ValidationContext
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.io.Source

class LarQualityEngineSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarQualityEngine {

  property("A LAR must pass quality checks") {
    for (x <- 1 to 5) {
      val lines = Source.fromFile(new File("parser/src/test/resources/txt/QualityMacroPasses_Test" + x + ".txt")).getLines()
      val lars = lines.drop(1).map(line => LarCsvParser(line)).collect {
        case Right(lar) => lar
      }

      lars.foreach { lar =>
        checkQuality(lar, ValidationContext(None, None)).isSuccess mustBe true
      }
    }
  }

}
