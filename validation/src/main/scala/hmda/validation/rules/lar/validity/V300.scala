package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateGeo._

object V300 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V300"

  override def apply(lar: LoanApplicationRegister): Result = {

    val msa = lar.geography.msa
    val tract = lar.geography.tract

    val validFormat = tract is validCensusTractFormat

    when(tract not equalTo("NA")) {
      validFormat and
        when(msa not equalTo("NA")) {
          (lar.geography is validCompleteCombination)
        } and
        when(msa is equalTo("NA")) {
          (lar.geography is validStateCountyTractCombination)
        }
    } and
      when(tract is equalTo("NA")) {
        (lar.geography is smallCounty) or (msa is equalTo("NA"))
      }

  }

}
