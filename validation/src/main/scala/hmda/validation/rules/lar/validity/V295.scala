package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateGeo._

object V295 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V295"

  override def apply(input: LoanApplicationRegister): Result = {

    val msa = input.geography.msa
    val county = input.geography.county

    val validCombination = when(county not equalTo("NA")) {
      input.geography is validStateCountyCombination
    }

    val NA = when(county is equalTo("NA")) {
      msa is equalTo("NA")
    }

    validCombination and NA

  }

}
