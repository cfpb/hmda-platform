package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V667_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V667-1"

  override def parent: String = "V667"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val invalidCreditScore = new InvalidCreditScoreCode
    // per discussion on 4233, this will not be updated to use an explicit list of relevant models,
    // and stay with exclusion logic as that is the intended effect
    when(lar.coApplicant.creditScoreType not oneOf(invalidCreditScore, OtherCreditScoreModel)) {
      lar.coApplicant.otherCreditScoreModel is empty
    } and when(lar.coApplicant.otherCreditScoreModel is empty) {
      lar.coApplicant.creditScoreType not oneOf(invalidCreditScore, OtherCreditScoreModel)
    }
  }
}
