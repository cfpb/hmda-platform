package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V700_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V700-1"

  override def parent: String = "V700"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val ausList       = List(lar.AUS.aus2, lar.AUS.aus3, lar.AUS.aus4, lar.AUS.aus5)
    val ausResultList = List(lar.ausResult.ausResult2, lar.ausResult.ausResult3, lar.ausResult.ausResult4, lar.ausResult.ausResult5)
    when(lar.AUS.aus1 is equalTo(AUSNotApplicable)) {
      (lar.ausResult.ausResult1 is equalTo(AutomatedUnderwritingResultNotApplicable)) and
        (ausList is equalTo(List(EmptyAUSValue, EmptyAUSValue, EmptyAUSValue, EmptyAUSValue))) and
        (ausResultList is equalTo(List(EmptyAUSResultValue, EmptyAUSResultValue, EmptyAUSResultValue, EmptyAUSResultValue)))
    }
  }
}
