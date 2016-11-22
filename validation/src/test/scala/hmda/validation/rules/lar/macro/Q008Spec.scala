package hmda.validation.rules.lar.`macro`
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q008Spec extends SimplifiedMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q008.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 2)
  override def relevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)

  simplifiedPropertyTests("withdrawn", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q008
}
