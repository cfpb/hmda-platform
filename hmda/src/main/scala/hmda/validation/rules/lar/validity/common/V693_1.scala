package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidApplicationSubmissionCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V693_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V693-1"

  override def parent: String = "V693"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.applicationSubmission not equalTo(new InvalidApplicationSubmissionCode)
}
