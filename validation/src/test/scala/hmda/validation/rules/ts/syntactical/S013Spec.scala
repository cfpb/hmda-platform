package hmda.validation.rules.ts.syntactical

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.Success
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

import scala.concurrent.{ ExecutionContext, Future }

class S013Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  property("timestamp must be later than timestamp in database") {
    forAll(tsGen) { ts =>
      val checkTimestamp = 201301111330L
      whenever(ts.id == 1) {
        S013(ts, checkTimestamp) mustBe Success()
        S013(ts, Future(checkTimestamp)).map(x => x mustBe Success())
      }
    }
  }

}
