package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q614_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q614-1"

  override def parent: String = "Q614"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val age = lar.applicant.age

    when(age not equalTo(8888)) {
        age is between(18, 100)
    }
  }
}
