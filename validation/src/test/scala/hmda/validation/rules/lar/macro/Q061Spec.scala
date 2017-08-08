package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q061Spec extends LessThanOrEqualToPropertyMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q061.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(rateSpread = "NA").copy(actionTakenType = 1)
  override def relevantLar(lar: LoanApplicationRegister) = {
    val newLoan = lar.loan.copy(propertyType = 1)
    lar.copy(actionTakenType = 1).copy(loan = newLoan).copy(rateSpread = "5.01").copy(lienStatus = 1).copy(actionTakenType = 1)
  }

  lessThanOrEqualToPropertyTests("first lien property type loans", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q061
}
