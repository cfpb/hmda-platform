package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q082Spec extends LessThanOrEqualToPropertyMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q082.numOfLarsMultiplier")

  override def relevantLar(lar: LoanApplicationRegister) = {
    val loan = lar.loan.copy(propertyType = 1)
    val applicant = lar.applicant.copy(sex = 3)
    lar.copy(actionTakenType = 1, loan = loan, applicant = applicant)
  }
  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)

  lessThanOrEqualToPropertyTests("relevant lars", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q082
}
