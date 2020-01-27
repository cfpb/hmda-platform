package hmda.validation.rules.lar.quality.twentytwenty

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q649_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q649_1"

  override def parent: String = "Q649"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(lar.applicant.creditScore not oneOf(7777, 8888, 1111)) {
        lar.applicant.creditScore is between(300, 900)
    }
  }
}
