package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V615_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V615_3

  property("Construction method must be correct") {
    forAll(larGen) { lar =>
      val list = List(ManufacturedHomeAndLand, ManufacturedHomeAndNotLand)
      whenever(list.contains(lar.property.manufacturedHomeSecuredProperty)) {
        val manufactured = lar.loan.copy(constructionMethod = ManufacturedHome)
        val notManufactured =
          lar.loan.copy(constructionMethod = new InvalidConstructionMethodCode)

        lar.copy(loan = manufactured).mustPass
        lar.copy(loan = notManufactured).mustFail
      }
    }
  }
}
