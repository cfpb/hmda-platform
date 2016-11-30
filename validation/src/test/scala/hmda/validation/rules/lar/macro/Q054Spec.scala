package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q054Spec extends LessThanOrEqualToPropertyMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q054.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 6, hoepaStatus = 2)
  override def relevantLar(lar: LoanApplicationRegister) = {
    lar.copy(actionTakenType = 6, hoepaStatus = 1, agencyCode = 5)
  }

  lessThanOrEqualToPropertyTests("hoepa loans", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q054
}
