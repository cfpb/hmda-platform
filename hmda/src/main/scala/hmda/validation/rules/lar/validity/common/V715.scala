package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V715 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V715"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val naf = lar.nonAmortizingFeatures

    when(
      naf.balloonPayment is equalTo(BalloonPaymentExempt) or
        (naf.interestOnlyPayments is equalTo(InterestOnlyPaymentExempt)) or
        (naf.negativeAmortization is equalTo(NegativeAmortizationExempt)) or
        (naf.otherNonAmortizingFeatures is equalTo(OtherNonAmortizingFeaturesExempt))
    ) {
      naf.balloonPayment is equalTo(BalloonPaymentExempt) and
        (naf.interestOnlyPayments is equalTo(InterestOnlyPaymentExempt)) and
        (naf.negativeAmortization is equalTo(NegativeAmortizationExempt)) and
        (naf.otherNonAmortizingFeatures is equalTo(OtherNonAmortizingFeaturesExempt))
    }
  }
}
