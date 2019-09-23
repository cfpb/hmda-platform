package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ EmptyAUSResultValue, EmptyAUSValue }
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V701 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V701"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val aus       = lar.AUS
    val ausResult = lar.ausResult
    when(aus.aus2 is equalTo(EmptyAUSValue)) {
      ausResult.ausResult2 is equalTo(EmptyAUSResultValue)
    } and
      when(aus.aus3 is equalTo(EmptyAUSValue)) {
        ausResult.ausResult3 is equalTo(EmptyAUSResultValue)
      } and
      when(aus.aus4 is equalTo(EmptyAUSValue)) {
        ausResult.ausResult4 is equalTo(EmptyAUSResultValue)
      } and
      when(aus.aus5 is equalTo(EmptyAUSValue)) {
        ausResult.ausResult5 is equalTo(EmptyAUSResultValue)
      }
  }
}
