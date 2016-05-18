package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object V400 extends EditCheck[LoanApplicationRegister] {

  val propertyTypes = List(1, 2, 3)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.loan.propertyType is containedIn(propertyTypes)
  }

  override def name: String = "V400"
}
