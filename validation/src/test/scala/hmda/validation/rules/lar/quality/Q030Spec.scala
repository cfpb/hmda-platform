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

  property("must pass when property is in a small county") {
    forAll(larGen) { lar =>
      val validGeography = Geography("10100", "46", "045", "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  // TODO is this true? do we assume it's a small county? if so, then what's the point?
  // if not, then how do we figure out whether reporting was in fact required for this property?
  property("must pass when county is NA") {

  }

  property("must fail when property is in a large county and filer is a CRA reporter") {

  }

}
