package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q055Spec extends LessThanOrEqualToPropertyMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q055.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(rateSpread = "NA", actionTakenType = 1)
  override def relevantLar(lar: LoanApplicationRegister) = {
    lar.copy(actionTakenType = 1, hoepaStatus = 1, rateSpread = "5")
  }

  lessThanOrEqualToPropertyTests("hoepa loans", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q055
}
