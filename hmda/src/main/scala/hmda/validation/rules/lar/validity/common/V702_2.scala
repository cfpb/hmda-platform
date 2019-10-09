package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.OtherAUS
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V702_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V702-2"

  override def parent: String = "V702"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.AUS.otherAUS not empty) {
      (lar.AUS.aus1 is equalTo(OtherAUS)) or
        (lar.AUS.aus2 is equalTo(OtherAUS)) or
        (lar.AUS.aus3 is equalTo(OtherAUS)) or
        (lar.AUS.aus4 is equalTo(OtherAUS)) or
        (lar.AUS.aus5 is equalTo(OtherAUS))
    }
}
