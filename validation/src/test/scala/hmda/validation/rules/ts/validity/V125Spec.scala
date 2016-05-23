package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class V125Spec extends TsEditCheckSpec with ValidityUtils {

  property("Correct tax id should pass") {
    forAll(tsGen) { ts =>
      ts.mustPass
    }
  }

  property("Incorrect tax id should not pass") {
    forAll(tsGen, Gen.alphaStr) { (ts, tax) =>
      val badTs = ts.copy(taxId = tax)
      badTs.mustFail
    }
  }

  property("Tax ID of the form 99-9999999 or 00-0000000 should not pass") {
    forAll(tsGen, Gen.oneOf("99-9999999", "00-0000000")) { (ts, tax) =>
      val badTs = ts.copy(taxId = tax)
      badTs.mustFail
    }
  }

  override def check: EditCheck[TransmittalSheet] = V125
}
