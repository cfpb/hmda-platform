package hmda.validation.engine.ts

import hmda.parser.fi.ts.TsGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import hmda.validation.dsl.Success
import org.scalatest.prop.PropertyChecks
import scala.concurrent.ExecutionContext

class TsEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with TsEngine with TsValidationApiSpec {
  override implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  property("Validates Transmittal Sheet") {
    forAll(tsGen) { ts =>
      val fValid = validateTs(ts, None)
      fValid.map(v => v mustBe Success())
    }
  }

}
