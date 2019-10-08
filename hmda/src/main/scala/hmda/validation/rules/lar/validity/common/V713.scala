package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ AUSExempt, AUSResultExempt, EmptyAUSResultValue, EmptyAUSValue }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V713 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V713"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.AUS.aus1 is equalTo(AUSExempt) or (lar.ausResult.ausResult1 is equalTo(AUSResultExempt))) {
      lar.AUS.aus1 is equalTo(AUSExempt) and
        (lar.ausResult.ausResult1 is equalTo(AUSResultExempt)) and
        (lar.AUS.aus2 is equalTo(EmptyAUSValue)) and
        (lar.AUS.aus3 is equalTo(EmptyAUSValue)) and
        (lar.AUS.aus4 is equalTo(EmptyAUSValue)) and
        (lar.AUS.aus5 is equalTo(EmptyAUSValue)) and
        (lar.AUS.otherAUS is empty) and
        (lar.ausResult.ausResult2 is equalTo(EmptyAUSResultValue)) and
        (lar.ausResult.ausResult3 is equalTo(EmptyAUSResultValue)) and
        (lar.ausResult.ausResult4 is equalTo(EmptyAUSResultValue)) and
        (lar.ausResult.ausResult5 is equalTo(EmptyAUSResultValue)) and
        (lar.ausResult.otherAusResult is empty)
    }
}
