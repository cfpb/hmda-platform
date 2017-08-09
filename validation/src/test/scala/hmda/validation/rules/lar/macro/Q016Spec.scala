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
    val underCapApplicant = lar.applicant.copy(income = (incomeCap - 1).toString)
    lar.copy(applicant = underCapApplicant)
  }
  def overCap(lar: LoanApplicationRegister) = {
    val overCapApplicant = lar.applicant.copy(income = (incomeCap + 1).toString)
    lar.copy(applicant = overCapApplicant)
  }

  def naIncome(lar: LoanApplicationRegister) = {
    val naApplicant = lar.applicant.copy(income = "NA")
    lar.copy(applicant = naApplicant)
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

  property(s"be valid if NA loans > $multiplier * over cap loans") {
    val numOfRelevantLars = sampleSizeTarget + 1
    val validLarSource = newLarSource(testLars, numOfRelevantLars, naIncome, overCap)
    validLarSource.mustPass
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q016
}
