package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q055Spec extends SimplifiedMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q055.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(rateSpread = "NA").copy(actionTakenType = 1)
  override def relevantLar(lar: LoanApplicationRegister) = {
    lar.copy(actionTakenType = 1).copy(hoepaStatus = 1).copy(rateSpread = "5").copy(actionTakenType = 1)
  }

  simplifiedPropertyTests("hoepa loans", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q055
}
