package hmda.validation.engine.ts

import hmda.parser.fi.ts.TsGenerators
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import scala.concurrent.{ ExecutionContext, Future }

class TsValidationEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with CommonTsValidationEngine with CommonTsValidationSpec {

  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  //TODO: Review, always passes
  //property("Transmittal Sheet fails S028 (Timestamp Format)") {
  //  forAll(tsGen) { ts =>
  //    whenever(ts.id == 1) {
  //      val testTs = ts.copy(timestamp = 201301111330L)
  //      failGenTs(testTs)
  //    }
  //  }
  //}
}
