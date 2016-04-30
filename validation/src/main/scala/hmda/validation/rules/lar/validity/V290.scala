package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V290 extends EditCheck[LoanApplicationRegister] {

  val cbsaTracts = CBSATractLookup.values

  val validCombinations = cbsaTracts.map { cbsa =>
    (cbsa.state, cbsa.county, cbsa.geoIdMsa)
  }

  val validMdCombinations = cbsaTracts.map { cbsa =>
    (cbsa.state, cbsa.county, cbsa.metDivFp)
  }

  override def name: String = "V290"

  override def apply(input: LoanApplicationRegister): Result = {
    val msa = input.geography.msa
    val state = input.geography.state
    val county = input.geography.county

    val combination = (state, county, msa)

    when(msa not equalTo("NA")) {
      (combination is containedIn(validCombinations)) or
        (combination is containedIn(validMdCombinations))
    }
  }

}
