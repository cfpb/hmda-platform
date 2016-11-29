package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q023Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q023.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size
  val sampleSizeTarget = (testLars.size * multiplier).toInt
  def irrelevantLar(lar: LoanApplicationRegister) = {
    val irrelevantGeography = lar.geography.copy(msa = "00000")
    lar.copy(geography = irrelevantGeography)
  }
  def relevantLar(lar: LoanApplicationRegister) = {
    val relevantGeography = lar.geography.copy(msa = "NA")
    lar.copy(geography = relevantGeography)
  }

  property(s"be valid if msa = NA < $multiplier * lars") {
    val numOfRelevantLars = sampleSizeTarget - 1
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be valid if msa = NA = $multiplier * lars") {
    val numOfRelevantLars = sampleSizeTarget
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be invalid if msa = NA > $multiplier * lars") {
    val numOfRelevantLars = sampleSizeTarget + 1
    val invalidLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q023
}
