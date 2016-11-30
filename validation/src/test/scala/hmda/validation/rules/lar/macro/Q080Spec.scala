package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q080Spec extends LessThanOrEqualToPropertyMacroSpec {

  val multiplier = config.getDouble("hmda.validation.macro.Q080.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSizeTarget = (testLars.size * multiplier).toInt
  def relevantLar(lar: LoanApplicationRegister) = {
    val loan = lar.loan.copy(propertyType = 1)
    val applicant = lar.applicant.copy(ethnicity = 3)
    lar.copy(actionTakenType = 1, loan = loan, applicant = applicant)
  }
  def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)

  lessThanOrEqualToPropertyTests("no ethnicity", multiplier, relevantLar, irrelevantLar)

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q080
}
