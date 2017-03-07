package hmda.validation.engine.ts.quality

import java.io.File

import hmda.parser.fi.ts.TsCsvParser
import hmda.validation.context.ValidationContext
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
  private val ctx = ValidationContext(None, None)

  property("A Transmittal Sheet must pass quality checks") {
    val line = Source.fromFile(new File("parser/jvm/src/test/resources/txt/clean_10-lars.txt")).getLines().take(1)
    val ts = line.map(l => TsCsvParser(l))

    ts.foreach { ts =>
      checkQuality(ts.right.get, ctx).isSuccess mustBe true
    }
  }
}
