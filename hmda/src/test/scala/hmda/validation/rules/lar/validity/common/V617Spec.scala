package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V617Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V617

  property("Loan amount must be valid") {
    forAll(larGen, Gen.choose(Double.MinValue, -1.0)) { (lar, a) =>
      lar.mustPass
      val badLoan = lar.loan.copy(amount = a)
      lar.copy(loan = badLoan).mustFail
    }
  }
}
