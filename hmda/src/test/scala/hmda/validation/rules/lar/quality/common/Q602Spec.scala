package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q602Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q602

  property(
    "If city, state and zip code are provided, street address should be provided") {
    forAll(larGen) { lar =>
      whenever(
        lar.geography.zipCode == "NA" || lar.geography.state == "NA" || lar.geography.city == "NA") {
        lar.mustPass
      }

      val applicableLar = lar.copy(
        geography =
          lar.geography.copy(zipCode = "test", city = "test", state = "test"))
      val invalidLar = applicableLar.copy(
        geography = applicableLar.geography.copy(street = "NA"))
      invalidLar.mustFail

      val validLar = applicableLar.copy(
        geography = applicableLar.geography.copy(street = "test"))
      validLar.mustPass
    }
  }
}
