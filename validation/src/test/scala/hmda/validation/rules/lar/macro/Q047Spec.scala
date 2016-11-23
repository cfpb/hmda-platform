package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q047Spec extends lessThanOrEqualToPropertyMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q047.numOfLarsMultiplier")

  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 2)
  override def relevantLar(lar: LoanApplicationRegister) = {
    lar.copy(actionTakenType = 4).copy(preapprovals = 1)
  }

  lessThanOrEqualToPropertyTests("preaproval accepted and application withdrawn", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q047
}
