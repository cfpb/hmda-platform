package hmda.validation.engine.lar.quality

import java.io.File

import hmda.parser.fi.lar.{ LarCsvParser, LarGenerators }
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
    // TODO: Ideally we'll use a clean file that will pass all quality/macro edits to test the quality engine.
    // This code is commented out until we have such a file

    /*
    val lines = Source.fromFile(new File("parser/src/test/resources/txt/FirstTestBankData_clean_407_2017.txt")).getLines()
    val lars = lines.drop(1).map(l => LarCsvParser(l))

    lars.foreach { lar =>
      checkQuality(lar).isSuccess mustBe true
    }*/
  }
}
