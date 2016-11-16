package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q055Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q055.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size
  def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 2)
  def relevantLar(lar: LoanApplicationRegister) = {
    lar.copy(actionTakenType = 1).copy(hoepaStatus = 1).copy(rateSpread = "5")
  }

  property(s"be valid if hoepa loans < $multiplier * total") {
    val numOfRelevantLars = (sampleSize * multiplier).toInt - 1
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be valid if hoepa loans = $multiplier * total") {
    val numOfRelevantLars = (sampleSize * multiplier).toInt
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be invalid if hoepa loans > $multiplier * total") {
    val numOfRelevantLars = (sampleSize * multiplier).toInt + 1
    val invalidLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q055
}
