package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q606Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q606

  property(
    "Income should be less than $10 million") {
    forAll(larGen) { lar =>
      lar.copy(income = "Text").mustPass
      lar.copy(income = "1").mustPass
      lar.copy(income = "9999").mustPass
      lar.copy(income = "10000").mustFail
      lar.copy(income = "10001").mustFail
    }
  }
}
