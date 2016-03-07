package hmda.validation.engine.ts

import hmda.parser.fi.ts.TsGenerators
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import scala.concurrent.{ ExecutionContext, Future }

class TsValidationEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with CommonTsValidationEngine with CommonTsValidationSpec {

  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit def badTimestamp: Gen[Int] = {
    Gen.oneOf(0, 1000)
  }

  property("Transmittal Sheet fails S028 (Timestamp Format)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = for {
          t <- badTimestamp
          x = ts.copy(timestamp = t)
        } yield x

        failGenTs(badTs)
      }
    }
  }
}
