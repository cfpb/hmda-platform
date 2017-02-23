package hmda.validation.rules.lar.`macro`
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q011Spec extends MacroSpec {

  val previousFixed = config.getInt("hmda.validation.macro.Q011.loan.previous.fixed")
  val multiplier = config.getInt("hmda.validation.macro.Q011.multiplier")




  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q011
}
