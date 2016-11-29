package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q048Spec extends LessThanOrEqualToPropertyMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q048.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)
  override def relevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 5, preapprovals = 1)

  lessThanOrEqualToPropertyTests("incomplete preapprovals", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q048
}
