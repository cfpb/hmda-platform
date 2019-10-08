package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V666_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V666-2"

  override def parent: String = "V666"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.creditScore is equalTo(9999)) {
      lar.coApplicant.creditScoreType is equalTo(CreditScoreNoCoApplicant)
    } and when(lar.coApplicant.creditScoreType is equalTo(CreditScoreNoCoApplicant)) {
      lar.coApplicant.creditScore is equalTo(9999)
    }
}
