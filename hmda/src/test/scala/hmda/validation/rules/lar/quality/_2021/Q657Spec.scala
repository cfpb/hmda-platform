package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q657Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q657
  val exemptCode=1111
  property("Non-exempt numeric field should not have a value of 1111") {
    forAll(larGen) { lar =>

      val appLar = lar.copy(loan = lar.loan.copy(loanType = InvalidLoanTypeExemptCode))
      appLar.mustFail
      appLar.copy(loan = lar.loan.copy(loanPurpose = InvalidLoanPurposeExemptCode)).mustFail
      appLar.copy(loan = lar.loan.copy(constructionMethod = InvalidConstructionMethodExemptCode)).mustFail
      appLar.copy(loan = lar.loan.copy(occupancy = InvalidOccupancyExemptCode)).mustFail
      appLar.copy(property = lar.property.copy(totalUnits = exemptCode)).mustFail

    }
  }
}
