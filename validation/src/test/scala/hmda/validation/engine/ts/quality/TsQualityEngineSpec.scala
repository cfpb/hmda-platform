package hmda.validation.engine.ts.quality

import java.io.File

import hmda.parser.fi.ts.TsCsvParser
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.concurrent.ExecutionContext
import scala.io.Source

class TsQualityEngineSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with TsQualityEngine {

  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  property("A LAR must pass quality checks") {
    for (x <- 1 to 5) {
      val line = Source.fromFile(new File("parser/src/test/resources/txt/QualityMacroPasses_Test" + x + ".txt")).getLines().take(1)
      val ts = line.map(l => TsCsvParser(l))

      ts.foreach { ts =>
        checkQuality(ts).isSuccess mustBe true
      }
    }
  }
}
