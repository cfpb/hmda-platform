package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.CreditScoreExempt
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.lar.validity.V710

object V710_1 extends V710 {
  override def parent: String = "V710"
  override def name: String = "V710-1"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    anyExemptionTaken(lar) {
      lar.applicant.creditScore is equalTo(1111) and
        (lar.applicant.creditScoreType is equalTo(CreditScoreExempt)) and
        (lar.coApplicant.creditScore is equalTo(1111)) and
        (lar.coApplicant.creditScoreType is equalTo(CreditScoreExempt))
    }
}
