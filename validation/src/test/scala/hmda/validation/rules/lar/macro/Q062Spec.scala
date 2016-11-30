package hmda.validation.rules.lar.`macro`
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q062Spec extends LessThanOrEqualToPropertyMacroSpec {
  override def multiplier: Double = config.getDouble("hmda.validation.macro.Q062.numOfLarsMultiplier")

  override def relevantLar(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(actionTakenType = 1, hoepaStatus = 1, lienStatus = 1, purchaserType = 1)
  }

  override def irrelevantLar(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(actionTakenType = 1, hoepaStatus = 2)
  }

  lessThanOrEqualToPropertyTests("originated HOEPA loans sold to Fannie Mae", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q062
}
