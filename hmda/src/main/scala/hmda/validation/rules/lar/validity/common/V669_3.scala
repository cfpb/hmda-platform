package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ EmptyDenialValue, InvalidDenialReasonCode }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V669_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V669-3"

  override def parent: String = "V669"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val filterList     = List(EmptyDenialValue, InvalidDenialReasonCode)
    val denialList     = List(lar.denial.denialReason1, lar.denial.denialReason2, lar.denial.denialReason3, lar.denial.denialReason4)
    val duplicateCheck = denialList.filterNot(filterList.contains(_))

    duplicateCheck.distinct.size is equalTo(duplicateCheck.size)
  }
}
