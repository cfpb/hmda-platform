package hmda.model.institution

import hmda.model.institution.Agency._
import org.scalatest.{ MustMatchers, WordSpec }

class AgencySpec extends WordSpec with MustMatchers {

  "Agency" must {

    "resolve Agency case class to the appropriate agencyId" in {
      CFPB.value mustBe 9
      FDIC.value mustBe 3
      FRS.value mustBe 2
      HUD.value mustBe 7
      NCUA.value mustBe 5
      OCC.value mustBe 1
      UndeterminedAgency.value mustBe -1
    }

    "resolve agencyId to the appropriate Agency case class" in {
      Agency.withValue(9) mustBe CFPB
      Agency.withValue(3) mustBe FDIC
      Agency.withValue(2) mustBe FRS
      Agency.withValue(7) mustBe HUD
      Agency.withValue(5) mustBe NCUA
      Agency.withValue(1) mustBe OCC
      Agency.withValue(-1) mustBe UndeterminedAgency
    }

    "must have entries for all agencies" in {
      Agency.values mustBe IndexedSeq(CFPB, FDIC, FRS, HUD, NCUA, OCC, UndeterminedAgency)
    }

  }

}
