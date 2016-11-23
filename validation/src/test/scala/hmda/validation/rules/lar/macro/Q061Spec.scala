package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q061Spec extends lessThanOrEqualToPropertyMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q061.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(rateSpread = "NA").copy(actionTakenType = 1)
  override def relevantLar(lar: LoanApplicationRegister) = {
    lar.copy(actionTakenType = 1).copy(hoepaStatus = 1).copy(rateSpread = "5").copy(lienStatus = 1).copy(actionTakenType = 1)
  }

  lessThanOrEqualToPropertyTests("first lien hoepa loans", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q061
}
