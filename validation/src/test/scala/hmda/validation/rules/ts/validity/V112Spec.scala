package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class V112Spec extends TsEditCheckSpec with ValidityUtils {

  property("Parent zip code must be valid") {
    forAll(tsGen) { ts =>
      ts.mustPass
    }
  }

  property("Empty zip code should pass") {
    forAll(tsGen) { ts =>
      val s1 = ts.parent.copy(zipCode = "")
      val goodTs1 = ts.copy(parent = s1)
      goodTs1.mustPass
    }
  }

  val badZipGen: Gen[String] = Gen.numStr.filter(_.length > 5)

  property("Wrong zip code should fail") {
    forAll(tsGen, badZipGen) { (ts, zip) =>
      val s2 = ts.parent.copy(zipCode = zip)
      val badTs2 = ts.copy(parent = s2)
      badTs2.mustFail
    }
  }

  override def check: EditCheck[TransmittalSheet] = V112
}
