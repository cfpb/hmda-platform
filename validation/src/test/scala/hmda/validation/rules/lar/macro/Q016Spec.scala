package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q016Spec extends MacroSpec {

  val multiplier = config.getDouble("hmda.validation.macro.Q016.numOfLarsMultiplier")
  val incomeCap = config.getInt("hmda.validation.macro.Q016.incomeCap")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size
  val sampleSizeTarget = (testLars.size * multiplier).toInt
  def underCap(lar: LoanApplicationRegister) = {
    val underCapLoan = lar.loan.copy(amount = incomeCap - 1)
    lar.copy(loan = underCapLoan)
  }
  def overCap(lar: LoanApplicationRegister) = {
    val overCapLoan = lar.loan.copy(amount = incomeCap + 1)
    lar.copy(loan = overCapLoan)
  }

  property(s"be valid if under cap loans < $multiplier * over cap loans") {
    val numOfRelevantLars = sampleSizeTarget - 1
    val validLarSource = newLarSource(testLars, numOfRelevantLars, underCap, overCap)
    validLarSource.mustPass
  }

  property(s"be invalid if under cap loans = $multiplier * over cap loans") {
    val numOfRelevantLars = sampleSizeTarget
    val validLarSource = newLarSource(testLars, numOfRelevantLars, underCap, overCap)
    validLarSource.mustPass
  }

  property(s"be invalid if under cap loans > $multiplier * over cap loans") {
    val numOfRelevantLars = sampleSizeTarget + 1
    val invalidLarSource = newLarSource(testLars, numOfRelevantLars, underCap, overCap)
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q016
}
