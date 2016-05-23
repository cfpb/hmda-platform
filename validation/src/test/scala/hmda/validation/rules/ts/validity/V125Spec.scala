package hmda.validation.rules.ts.validity

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class V125Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with ValidityUtils {

  property("Correct tax id should pass") {
    forAll(tsGen) { ts =>
      whenever(ts.taxId.length == 10) {
        V125(ts) mustBe Success()
      }
    }
  }

  property("Incorrect tax id should not pass") {
    forAll(tsGen, Gen.alphaStr) { (ts, tax) =>
      val badTs = ts.copy(taxId = tax)
      V125(badTs) mustBe Failure("wrong number")
    }
  }

  property("Tax ID of the form 99-9999999 or 00-0000000 should not pass") {
    forAll(tsGen, Gen.oneOf("99-9999999", "00-0000000")) { (ts, tax) =>
      val badTs = ts.copy(taxId = tax)
      V125(badTs) mustBe Failure("wrong number")
    }

  }
}
