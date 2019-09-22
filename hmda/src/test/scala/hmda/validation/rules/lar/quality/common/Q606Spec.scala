package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._

class Q606Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q606

  property("Income must be less than 3000 if numeric") {
    forAll(larGen) { lar =>
      whenever(lar.income == "NA") {
        lar.mustPass
      }

      lar.copy(income = "2999").mustPass
      lar.copy(income = "3000").mustFail
      lar.copy(income = "3001").mustFail
    }
  }
}
