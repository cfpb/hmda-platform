package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ RegexDsl, Result, Success }
import hmda.validation.rules.EditCheck

object V300 extends EditCheck[LoanApplicationRegister] with RegexDsl {

  val cbsaTracts = CBSATractLookup.values

  val validCombination = cbsaTracts.map { cbsa =>
    (cbsa.geoidMsa, cbsa.state, cbsa.county, cbsa.tractDecimal)
  }

  override def name: String = "V300"

  override def apply(input: LoanApplicationRegister): Result = {

    val msa = input.geography.msa
    val state = input.geography.state
    val county = input.geography.county
    val tract = input.geography.tract

    val combination = (msa, state, county, tract)

    val validCensusTractCombination = when(msa not equalTo("NA")) {
      combination is containedIn(validCombination)
    }

    val validFormat = (tract is validCensusTractFormat) or (tract is equalTo("NA"))

    validCensusTractCombination and validFormat

  }

}
