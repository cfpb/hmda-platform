package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateGeo._

object V290 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V290"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.geography.msa not equalTo("NA")) {
      lar.geography is validStateCountyMsaCombination
    }
  }

}
