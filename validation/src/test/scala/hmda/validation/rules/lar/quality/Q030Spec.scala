package hmda.validation.rules.lar.quality

import hmda.census.model.TractLookup
import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class Q030Spec extends LarEditCheckSpec {
  property("passes when action take is not in range") {
    forAll(larGen, Gen.choose(7, 9)) { (lar, i) =>
      val validLar = lar.copy(actionTakenType = i)
      validLar.mustPass
    }
  }

  property("passes when action taken is in range and geography is not NA") {
    forAll(larGen, Gen.choose(1, 6), stringOfN(3, Gen.alphaChar)) { (lar, i, geo) =>
      val validGeo = Geography(geo, geo, geo, geo)
      val validLar = lar.copy(actionTakenType = i, geography = validGeo)
      validLar.mustPass
    }
  }

  property("fails when action taken is in range and tract, state or county is NA") {
    val geoGen = Gen.oneOf[Geography](
      Geography("1", "NA", "2", "3"),
      Geography("1", "2", "NA", "3"),
      Geography("1", "2", "3", "NA")
    )
    forAll(larGen, Gen.choose(1, 6), geoGen) { (lar, i, g) =>
      val invalidLar = lar.copy(actionTakenType = i, geography = g)
      invalidLar.mustFail
    }
  }

  property("succeeds when action taken is in range, MSA is NA, and tract is in not MSA") {
    val msaNa = TractLookup.values.filter(t => t.msa == "99999").toSeq
    val geoGen = Gen.oneOf(msaNa)
    forAll(larGen, Gen.choose(1, 6), geoGen) { (lar, i, g) =>
      val validLar = lar.copy(actionTakenType = i, geography = Geography("NA", g.state, g.county, g.tractDec))
      validLar.mustPass
    }
  }

  property("fails when action taken is in range, MSA is NA, and tract is in MSA") {
    val msaNa = TractLookup.values.filter(t => t.msa != "99999").toSeq
    val geoGen = Gen.oneOf(msaNa)
    forAll(larGen, Gen.choose(1, 6), geoGen) { (lar, i, g) =>
      val invalidLar = lar.copy(actionTakenType = i, geography = Geography("NA", g.state, g.county, g.tractDec))
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q030
}
