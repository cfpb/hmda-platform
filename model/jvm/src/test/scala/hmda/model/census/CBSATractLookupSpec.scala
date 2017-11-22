package hmda.model.census

import org.scalatest.{ MustMatchers, WordSpec }

class CBSATractLookupSpec extends WordSpec with MustMatchers {

  "CBSA Tract lookup" must {
    "Provide lookup between MSA/MD, state and counties" in {
      val lookup = CBSATractLookup.values
      val msaList = lookup.filter(c => c.geoIdMsa == "10100")
      msaList.size mustBe 11
      msaList.head.name mustBe "Aberdeen, SD"
    }
  }

}
