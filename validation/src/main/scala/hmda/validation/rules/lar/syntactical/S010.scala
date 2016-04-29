package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object S010 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateSyntax._
  import hmda.validation.dsl.PredicateDefaults._

  //Hardcoded for now
  val larId = 2

  def apply(lar: LoanApplicationRegister): Result = {
    lar.id is equalTo(larId)
  }

  def name = "S010"
}
