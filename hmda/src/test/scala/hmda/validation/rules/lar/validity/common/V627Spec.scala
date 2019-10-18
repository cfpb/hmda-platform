package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.census.records.CensusRecords

class V627Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V627.withIndexedCounties(CensusRecords.indexedCounty2018)

  property("Census County must be valid") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        geography = lar.geography.copy(county = "NA", tract = "NA")
      )
      unappLar.mustPass

      val lowerLar = lar.copy(
        geography = lar.geography.copy(county = "na", tract = "na")
      )
      lowerLar.mustFail

      val spaceLar = lar.copy(
        geography = lar.geography.copy(county = " ", tract = " ")
      )
      spaceLar.mustFail

      val emptyTract = lar.copy(
        geography = lar.geography.copy(county = "NA", tract = "")
      )
      emptyTract.mustPass

      val emptyCounty = lar.copy(
        geography = lar.geography.copy(county = "", tract = "NA")
      )
      emptyCounty.mustPass

      val nonMatching = lar.copy(
        geography = lar.geography.copy(county = "12345", tract = "54321678910")
      )
      nonMatching.mustFail

      val wrongFipsCode = lar.copy(
        geography = lar.geography.copy(county = "12345", tract = "12345678910")
      )
      wrongFipsCode.mustFail

      val matchingAndCorrectFipsCode = lar.copy(
        geography = lar.geography.copy(county = "01001", tract = "01001020100")
      )
      matchingAndCorrectFipsCode.mustPass
    }
  }
}
