package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class V145Spec extends TsEditCheckSpec with ValidityUtils {
  property("Succeeds for respondents with valid 5-digit ZIP codes") {
    forAll(tsGen, zip5Gen) { (ts, zip) =>
      val newRespondent = ts.respondent.copy(zipCode = zip)
      val newTs = ts.copy(respondent = newRespondent)
      newTs.mustPass
    }
  }

  property("Succeeds for respondents with valid 9-digit ZIP codes") {
    forAll(tsGen, zipPlus4Gen) { (ts, zip) =>
      val newRespondent = ts.respondent.copy(zipCode = zip)
      val newTs = ts.copy(respondent = newRespondent)
      newTs.mustPass
    }
  }

  property("Fails for respondents with numeric ZIP of the wrong length") {
    forAll(tsGen, invalidZipGen) { (ts, zip) =>
      val invalidRespondent = ts.respondent.copy(zipCode = zip)
      val invalidTs = ts.copy(respondent = invalidRespondent)
      invalidTs.mustFail
    }
  }

  property("Fails for respondents with any other string in ZIP") {
    forAll(tsGen, Gen.alphaStr) { (ts, zip) =>
      val invalidRespondent = ts.respondent.copy(zipCode = zip)
      val invalidTs = ts.copy(respondent = invalidRespondent)
      invalidTs.mustFail
    }
  }

  override def check: EditCheck[TransmittalSheet] = V145
}
