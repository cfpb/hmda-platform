package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V295 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  val cbsaTracts = CBSATractLookup.values

  val validCombination = cbsaTracts.map { cbsa =>
    (cbsa.state, cbsa.county)
  }.toSet

  override def name: String = "V295"

  override def apply(input: LoanApplicationRegister): Result = {

    val msa = input.geography.msa
    val state = input.geography.state
    val county = input.geography.county

    val combination = (state, county)

    val validStateCountyCombination = when(county not equalTo("NA")) {
      combination is containedIn(validCombination)
    }

    val NA = when(county is equalTo("NA")) {
      msa is equalTo("NA")
    }

    validStateCountyCombination and NA

  }

}
