package hmda.validation.rules.ts.validity

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class V140Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with ValidityUtils {

  property("Respondent state code must equal a valid postal code abbreviation") {
    forAll(tsGen) { ts =>
      val r = ts.respondent
      whenever(respondentNotEmpty(r)) {
        V140(ts) mustBe Success()
      }
    }
  }

  property("Wrong or missing respondent state code should fail") {
    forAll(tsGen) { ts =>
      val r1 = ts.respondent.copy(state = "")
      val badTs1 = ts.copy(respondent = r1)
      val r2 = ts.respondent.copy(state = "XXX")
      val badTs2 = ts.copy(respondent = r2)
      whenever(ts.id == 1) {
        V140(badTs1) mustBe Failure("is not contained in valid values domain")
        V140(badTs2) mustBe Failure("is not contained in valid values domain")
      }
    }
  }
}
