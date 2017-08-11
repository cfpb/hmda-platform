package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q010Spec extends MacroSpec {

  val multiplier = config.getDouble("hmda.validation.macro.Q010.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size
  val sampleSizeTarget = (testLars.size * multiplier).toInt
  def originated(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 1)
  def nonPreapprovals(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 4)

  property(s"be valid if originated > $multiplier * nonPreapprovals") {
    val numOfRelevantLars = sampleSizeTarget + 1
    val validLarSource = newLarSource(testLars, numOfRelevantLars, originated, nonPreapprovals)
    validLarSource.mustPass
  }

  property(s"be valid if originated = $multiplier * nonPreapprovals") {
    val numOfRelevantLars = sampleSizeTarget
    val validLarSource = newLarSource(testLars, numOfRelevantLars, originated, nonPreapprovals)
    validLarSource.mustPass
  }

  property(s"be invalid if originated < $multiplier * nonPreapprovals") {
    val numOfRelevantLars = sampleSizeTarget - 1
    val invalidLarSource = newLarSource(testLars, numOfRelevantLars, originated, nonPreapprovals)
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q010
}
