package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }

object V400 extends CommonDsl {

  val propertyTypes = List(1, 2, 3)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.loan.propertyType is containedIn(propertyTypes)
  }

}
