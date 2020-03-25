package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q649_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q649-1"

  override def parent: String = "Q649"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(lar.applicant.creditScore not oneOf(9999, 7777, 8888, 1111)) {
        lar.applicant.creditScore is between(300, 900)
    }
  }
}
