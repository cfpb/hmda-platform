package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V295 extends EditCheck[LoanApplicationRegister] {

  val cbsaTracts = CBSATractLookup.values

  override def name: String = "V295"

  def failureMessage = "State and county does not equal a valid combination or (county equals NA and MSA/MD not NA)"

  override def apply(input: LoanApplicationRegister): Result = {

    val msa = input.geography.msa
    val state = input.geography.state
    val county = input.geography.county
    val countyFips = input.geography.state + county

    val combination = (state, countyFips)

    val validCombination = cbsaTracts.map { cbsa =>
      (cbsa.state, cbsa.county)
    }

    val validStateCountyCombination = combination is containedIn(validCombination)

    val NA = when(county is equalTo("NA")) {
      msa is equalTo("NA")
    }

    if (county == "NA")
      NA
    else
      validStateCountyCombination

  }

}
