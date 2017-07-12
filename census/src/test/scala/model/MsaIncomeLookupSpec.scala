package model

import hmda.census.model.{ MsaIncome, MsaIncomeLookup }
import org.scalatest.{ MustMatchers, WordSpec }

class MsaIncomeLookupSpec extends WordSpec with MustMatchers {

  "MsaIncome Lookup" must {
    "find income for a particular MSA" in {
      val lookup = MsaIncomeLookup.values
      val msa = lookup.find(m => m.fips == 11540).getOrElse(MsaIncome())
      msa.name mustBe "APPLETON, WI"
      msa.income mustBe 73709
    }
  }
}
