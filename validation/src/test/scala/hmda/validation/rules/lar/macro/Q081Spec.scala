package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q081Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q081.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSizeTarget = (testLars.size * multiplier).toInt
  def relevantLar(lar: LoanApplicationRegister) = {
    val loan = lar.loan.copy(propertyType = 1)
    val applicant = lar.applicant.copy(race1 = 6)
    lar.copy(actionTakenType = 1, loan = loan, applicant = applicant)
  }
  def deniedLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)

  property(s"be valid if relevant lars < $multiplier * approved or denied lars") {
    val numOfRelevantLars = sampleSizeTarget - 1
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, deniedLar)
    validLarSource.mustPass
  }

  property(s"be valid if relevant lars = $multiplier * approved or denied lars") {
    val numOfRelevantLars = sampleSizeTarget
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, deniedLar)
    validLarSource.mustPass
  }

  property(s"be invalid if relevant lars > $multiplier * approved or denied lars") {
    val numOfRelevantLars = sampleSizeTarget + 1
    val invalidLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, deniedLar)
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q081
}
