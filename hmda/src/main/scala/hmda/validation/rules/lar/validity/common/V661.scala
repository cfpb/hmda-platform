package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V661 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V661"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.creditScore is equalTo(8888)) {
      lar.applicant.creditScoreType is equalTo(CreditScoreNotApplicable)
    } and when(lar.applicant.creditScoreType is equalTo(CreditScoreNotApplicable)) {
      lar.applicant.creditScore is equalTo(8888)
    }
}
