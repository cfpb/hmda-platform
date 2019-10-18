package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.census.records.CensusRecords

class V626Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V626.withIndexedCounties(CensusRecords.indexedCounty2018)

  property("Census County must be valid") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        geography = lar.geography.copy(county = "NA")
      )
      unappLar.mustPass

      val lowerLar = lar.copy(
        geography = lar.geography.copy(county = "na")
      )
      lowerLar.mustFail

      val appLar = lar.copy(geography = lar.geography.copy(county = "1"))
      appLar.mustFail

      val emptyCounty = lar.copy(geography = lar.geography.copy(county = ""))
      emptyCounty.mustFail

      val invalidCountyAllDigits =
        lar.copy(geography = lar.geography.copy(county = "00000"))
      invalidCountyAllDigits.mustFail

      val invalidCountyString =
        lar.copy(geography = lar.geography.copy(county = "N/AN/"))
      invalidCountyString.mustFail

      val validFipsCounty =
        lar.copy(geography = lar.geography.copy(county = "01001"))
      validFipsCounty.mustPass
    }
  }
}
