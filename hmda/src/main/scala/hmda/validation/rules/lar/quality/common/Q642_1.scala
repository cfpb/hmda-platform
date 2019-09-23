package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ OneOrMoreCreditScoreModels, OtherCreditScoreModel }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q642_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q642-1"

  override def parent: String = "Q642"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.creditScore is equalTo(7777)) {
      lar.applicant.creditScoreType is oneOf(OneOrMoreCreditScoreModels, OtherCreditScoreModel)
    }
}
