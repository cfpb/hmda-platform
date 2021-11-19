package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V665_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V665-2"

  override def parent: String = "V665"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.coApplicant.creditScoreType not equalTo(new InvalidCreditScoreCode)
}
