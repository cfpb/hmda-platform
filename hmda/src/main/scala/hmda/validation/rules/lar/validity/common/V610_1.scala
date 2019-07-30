package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateHmda._

object V610_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V610-1"

  override def parent: String = "V610"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val applicationDate = lar.loan.applicationDate
    (applicationDate is validDateFormat) or
      (applicationDate is equalTo("NA"))
  }

}
