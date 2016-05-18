package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.validation.rules.EditCheck

object V250 extends EditCheck[LoanApplicationRegister] {

  //the numeric clause is covered by the parser
  override def apply(lar: LoanApplicationRegister): Result = {
    (lar.loan.amount is numeric) and (lar.loan.amount is greaterThan(0))
  }

  override def name = "V250"
}