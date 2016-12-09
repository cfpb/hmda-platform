package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateGeo._

object V300 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V300"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

  override def apply(lar: LoanApplicationRegister): Result = {

    val msa = lar.geography.msa
    val tract = lar.geography.tract

    when(tract not equalTo("NA")) {
      tract is validCensusTractFormat and
        when(msa not equalTo("NA")) {
          lar.geography is validCompleteCombination
        } and
        when(msa is equalTo("NA")) {
          lar.geography is validStateCountyTractCombination
        }
    } and
      when(tract is equalTo("NA")) {
        (lar.geography is smallCounty) or (msa is equalTo("NA"))
      }

  }

}
