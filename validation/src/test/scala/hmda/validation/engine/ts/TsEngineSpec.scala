package hmda.validation.engine.ts

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.context.ValidationContext
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.concurrent.ExecutionContext
import scalaz.Success

class TsEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with TsEngine {
  override implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  property("Validates Transmittal Sheet") {
    forAll(tsGen) { ts =>
      validateTs(ts, ValidationContext(None, None)) mustBe a[Success[_]]
    }
  }

}
