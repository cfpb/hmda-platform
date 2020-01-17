package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidConstructionMethodCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V615_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V615_1

  property("Construction method must be valid") {
    forAll(larGen) { l =>
      l.mustPass
      val badLoan =
        l.loan.copy(constructionMethod = new InvalidConstructionMethodCode)
      l.copy(loan = badLoan).mustFail
    }
  }

}
