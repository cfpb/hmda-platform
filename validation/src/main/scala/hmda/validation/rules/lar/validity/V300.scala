package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ RegexDsl, Result, Success }
import hmda.validation.rules.EditCheck

object V300 extends EditCheck[LoanApplicationRegister] with RegexDsl {

  val cbsaTracts = CBSATractLookup.values

  override def name: String = "V300"

  def failureMessage = "Census tract not in valid format or is missing, does not equal NA, or does not equal a valid census tract number"

  override def apply(input: LoanApplicationRegister): Result = {
    val validFormat = input.geography.tract is validCensusTractFormat

    val msa = input.geography.msa
    val state = input.geography.state
    val county = state + input.geography.county
    val tract = input.geography.tract

    val combination = (msa, state, county, tract)

    val validCombination = cbsaTracts.map { cbsa =>
      (cbsa.geoidMsa, cbsa.state, cbsa.county, cbsa.tracts)
    }

    val validCensusTractCombination = combination is containedIn(validCombination)

    validCensusTractCombination

    //validFormat and validCensusTractCombination
  }

}
