package hmda.validation.rules.lar.validity._2020
import hmda.census.records.CensusRecords
import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity._2020.V716

class V716Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V716.withIndexedCounties(CensusRecords.indexedCounty2019) //TODO This should be updated for 2020

  property(
    "2 letter state code shall match 2 digit FIPS state code reported as the first two digits of the County") {
    forAll(larGen) { lar =>
      whenever(lar.geography.county == "NA" || lar.geography.state == "NA") {
        lar.mustPass
      }

      val applicableLar = lar.copy(geography = lar.geography.copy(state = "VA"))
      val invalidLar = applicableLar.copy(
        geography = applicableLar.geography.copy(county = "12345"))
      invalidLar.mustFail

      val validLar = applicableLar.copy(
        geography = applicableLar.geography.copy(county = "51810"))
      validLar.mustPass
    }
  }
}
