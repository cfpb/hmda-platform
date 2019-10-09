package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.OtherAutomatedUnderwritingResult
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V703_2 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V703-2"

  override def parent: String = "V703"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.ausResult.otherAusResult not empty) {
      (lar.ausResult.ausResult1 is equalTo(OtherAutomatedUnderwritingResult)) or
        (lar.ausResult.ausResult2 is equalTo(OtherAutomatedUnderwritingResult)) or
        (lar.ausResult.ausResult3 is equalTo(OtherAutomatedUnderwritingResult)) or
        (lar.ausResult.ausResult4 is equalTo(OtherAutomatedUnderwritingResult)) or
        (lar.ausResult.ausResult5 is equalTo(OtherAutomatedUnderwritingResult))
    }
}
