package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._

object V500 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V500"

  override def apply(lar: LoanApplicationRegister): Result = {
    (lar.rateSpread is equalTo("NA")) or (lar.rateSpread is numericMatching("NN.NN"))
  }
}
