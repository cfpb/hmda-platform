package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q081Spec extends SimplifiedMacroSpec {

  override val multiplier = config.getDouble("hmda.validation.macro.Q081.numOfLarsMultiplier")

  override def relevantLar(lar: LoanApplicationRegister) = {
    val loan = lar.loan.copy(propertyType = 1)
    val applicant = lar.applicant.copy(race1 = 6)
    lar.copy(actionTakenType = 1, loan = loan, applicant = applicant)
  }
  override def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)

  simplifiedPropertyTests("relevant lars", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q081
}
