package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidBalloonPaymentCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V684 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V684"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.nonAmortizingFeatures.balloonPayment not equalTo(new InvalidBalloonPaymentCode)
}
