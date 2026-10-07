package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.lar.validity.V710

object V710_2 extends V710 {
  override def parent: String = "V710"
  override def name: String = "V710-2"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    anyExemptionTaken(lar) {
        lar.applicant.otherCreditScoreModel is empty and
        (lar.coApplicant.otherCreditScoreModel is empty)
    }
}
