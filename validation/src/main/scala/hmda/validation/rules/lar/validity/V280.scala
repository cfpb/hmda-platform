package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V280 extends EditCheck[LoanApplicationRegister] with CensusEditCheck {

  val validMSAs = cbsaTracts.map(cbsa => cbsa.geoidMsa)

  override def name: String = "V280"

  override def apply(input: LoanApplicationRegister): Result = {
    val msa = msaCode(input.geography.msa)

    val NA = msa is equalTo("NA")

    val validMSA = msa is containedIn(validMSAs)

    validMSA or NA
  }
}
