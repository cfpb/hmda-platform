package hmda.validation.engine.lar.quality

import java.io.File

import hmda.model.fi.lar.LarGenerators
import hmda.parser.fi.lar.LarCsvParser
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
    val lines = Source.fromFile(new File("parser/jvm/src/test/resources/txt/clean_5508-lars.txt")).getLines()
    val lars = lines.drop(1).map(line => LarCsvParser(line)).collect {
      case Right(lar) => lar
    }

    lars.foreach { lar =>
      checkQuality(lar, ValidationContext(None, None)).isSuccess mustBe true
    }
  }

}
