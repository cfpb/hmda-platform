package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateCommon._

object S010 extends EditCheck[LoanApplicationRegister] {

  //Hardcoded for now
  val larId = 2

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.id is equalTo(larId)
  }

  override def name = "S010"
}
