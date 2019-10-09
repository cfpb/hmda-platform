package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V615_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V615-2"

  override def parent: String = "V615"

  val propertyInterest =
    List(DirectOwnership, IndirectOwnership, PaidLeasehold, UnpaidLeasehold)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.manufacturedHomeLandPropertyInterest is containedIn(propertyInterest)) {
      lar.loan.constructionMethod is equalTo(ManufacturedHome)
    }

}
