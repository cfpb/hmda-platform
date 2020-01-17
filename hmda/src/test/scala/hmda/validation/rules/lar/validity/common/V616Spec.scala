package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidOccupancyCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V616Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V616

  property("Occupancy must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val badLoan = lar.loan.copy(occupancy = new InvalidOccupancyCode)
      lar.copy(loan = badLoan).mustFail
    }
  }
}
