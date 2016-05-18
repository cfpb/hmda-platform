package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V285 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  val cbsaTracts = CBSATractLookup.values

  val stateCodes = cbsaTracts.map(c => c.state).toSet

  override def name: String = "V285"

  override def apply(input: LoanApplicationRegister): Result = {
    val state = input.geography.state

    val msa = input.geography.msa

    val validState = state is containedIn(stateCodes)

    val NA = when(state is equalTo("NA")) {
      msa is equalTo("NA")
    }

    val checkState = when(state not equalTo("NA")) {
      validState
    }

    checkState and NA

  }

}
