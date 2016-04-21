package hmda.model.census

import org.scalatest.{ MustMatchers, WordSpec }

class CBSATractLookupSpec extends WordSpec with MustMatchers {

  "CBSA Tract lookup" must {
    "Provide lookup between MSA/MD, state and counties" in {
      val lookup = CBSATractLookup()
      val msaList = lookup.filter(c => c.geoid_msa == "10100")
      msaList.size mustBe 10
      msaList.head.name mustBe "Aberdeen, SD"
      msaList.head.tract mustBe "46013951300"
    }
  }

}
