package hmda.validation.rules.lar.validity

import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V667_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V667-1"

  override def parent: String = "V667"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(
      lar.coApplicant.creditScoreType not oneOf(InvalidCreditScoreCode,
                                                OtherCreditScoreModel)) {
      lar.coApplicant.otherCreditScoreModel is empty
    } and when(lar.coApplicant.otherCreditScoreModel is empty) {
      lar.coApplicant.creditScoreType not oneOf(InvalidCreditScoreCode,
                                                OtherCreditScoreModel)
    }
  }
}
