package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V662_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V662-2"

  override def parent: String = "V662"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.creditScoreType is equalTo(OtherCreditScoreModel)) {
      lar.applicant.otherCreditScoreModel not empty
    } and when(lar.applicant.otherCreditScoreModel not empty) {
      lar.applicant.creditScoreType is equalTo(OtherCreditScoreModel)
    }
}
