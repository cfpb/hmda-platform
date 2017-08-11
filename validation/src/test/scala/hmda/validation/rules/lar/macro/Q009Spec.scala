package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q009Spec extends MacroSpec {

  val multiplier = config.getDouble("hmda.validation.macro.Q009.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size
  val sampleSizeTarget = (testLars.size * multiplier).toInt
  def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)
  def relevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 5)

  property(s"be valid if incomplete lars < $multiplier * lars") {
    val numOfRelevantLars = sampleSizeTarget - 1
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be valid if incomplete lars = $multiplier * lars") {
    val numOfRelevantLars = sampleSizeTarget
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be invalid if incomplete lars > $multiplier * lars") {
    val numOfRelevantLars = sampleSizeTarget + 1
    val invalidLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q009
}
