package hmda.validation.rules.lar.`macro`
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q063Spec extends LessThanOrEqualToPropertyMacroSpec {
  override def multiplier: Double = config.getDouble("hmda.validation.macro.Q063.numOfLarsMultiplier")

  override def relevantLar(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(actionTakenType = 1, hoepaStatus = 1, lienStatus = 1, purchaserType = 3)
  }

  override def irrelevantLar(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(actionTakenType = 1, lienStatus = 2)
  }

  lessThanOrEqualToPropertyTests("originated HOEPA loans sold to Freddie Mac", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q063
}
