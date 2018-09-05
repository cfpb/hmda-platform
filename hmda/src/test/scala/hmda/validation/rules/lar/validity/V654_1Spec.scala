package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import org.scalacheck.Gen
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V654_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V654_1

  property("Income must be valid") {
    forAll(larGen, Gen.alphaStr) { (lar, i) =>
      lar.mustPass
      val badIncome =
        lar.copy(income = i)
      badIncome.mustFail
    }
  }
}
