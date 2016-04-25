package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V295 extends EditCheck[LoanApplicationRegister] {

  val cbsaTracts = CBSATractLookup.values

  override def name: String = "V295"

  def failureMessage = ""

  override def apply(input: LoanApplicationRegister): Result = {

    val state = input.geography.state
    val county = input.geography.county
    val countyFips = input.geography.state + county

    val NA = when(input.geography.msa is equalTo("NA")) {
      county is equalTo("NA")
    }

    val combination = (state, countyFips)

    val validCombination = cbsaTracts.map { cbsa =>
      (cbsa.state, cbsa.county)
    }

    val validStateCountyCombination = combination is containedIn(validCombination)

    validStateCountyCombination or NA

  }

}
