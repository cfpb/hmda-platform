package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalatest.WordSpecLike

class Q030Spec extends LarEditCheckSpec {

  var institution: Institution = _
  override def check: EditCheck[LoanApplicationRegister] = Q030.inContext(ValidationContext(Some(institution)))

  property("must pass when action taken = 7 or 8 (preapproval)") {
    forAll(larGen) { lar =>
      whenever(Set(7, 8).contains(lar.actionTakenType)) {
        lar.mustPass
      }
    }
  }

  property("must pass when property MSA/MD, state, and county match, and tract is NA (as happens in small counties)") {
    forAll(larGen) { lar =>
      val validGeography = Geography("10100", "46", "045", "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  ignore("must pass when property MSA/MD, state, county, and census tract match") {
    forAll(larGen) { lar =>
      val validGeography = Geography("17020", "06", "007", "0036.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("must pass when property MSA/MD is NA, and state, county, and census tract match") {
  }

  property("must pass when property MSA/MD and tract are NA, and state and county match") {
  }

  // TODO failure cases for "populated but not matching" for each of the above. unless we can assume validity edits catch it...

  property("must pass when filer is non-CRA and property geo fields are all NA") {
  }

  property("must FAIL when filer is a CRA reporter and property geo fields are all NA") {
  }

  property("must FAIL when filer is a CRA reporter and state or county is missing") {
  }

}
