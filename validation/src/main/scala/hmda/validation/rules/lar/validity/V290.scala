package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Result }
import hmda.validation.rules.EditCheck

object V290 extends EditCheck[LoanApplicationRegister] {

  val cbsaTracts = CBSATractLookup.values

  override def name: String = "V290"

  def failureMessage = "MSA/MD, state, and county codes do not = a valid combination"

  override def apply(input: LoanApplicationRegister): Result = {
    val msa = msaCode(input.geography.msa)
    val state = input.geography.state
    val county = state + input.geography.county

    when(msa not equalTo("NA")) {
      val values = cbsaTracts.filter { c =>
        c.geoidMsa == msa &&
          c.state == state &&
          c.county == county
      }
      if (values.nonEmpty) {
        val validCombination = values.head
        val validState = validCombination.state
        val validCounty = validCombination.county
        validState is equalTo(state) and
          (validCounty is equalTo(county))
      } else {
        Failure(failureMessage)
      }
    }
  }

  private def msaCode(code: String): String = {
    val md = cbsaTracts.filter(m => m.metdivfp == code)
    if (md.nonEmpty) {
      md.head.geoidMsa
    } else {
      code
    }
  }
}
