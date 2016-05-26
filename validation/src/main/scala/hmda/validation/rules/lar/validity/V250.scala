package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V250 extends EditCheck[LoanApplicationRegister] {

  // The parser ensures loan amount is numeric, so the first clause
  //  of this edit check will never fail. (If it is not numeric,
  //  file will not parse.)
  override def apply(lar: LoanApplicationRegister): Result = {
    (lar.loan.amount is numeric) and (lar.loan.amount is greaterThan(0))
  }

  override def name = "V250"
}