package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q047Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q047.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size
  def validLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 2)
  def invalidLar(lar: LoanApplicationRegister) = {
    lar.copy(actionTakenType = 4).copy(preapprovals = 1)
  }

  property(s"be valid if single family and withdrawn < $multiplier * total") {
    val numOfGoodLars = (sampleSize * (1.0 - multiplier)).toInt + 1
    val validLarSource = newLarSource(testLars, numOfGoodLars, validLar(_), invalidLar(_))
    validLarSource.mustPass
  }

  property(s"be valid if single family and withdrawn = $multiplier * total") {
    val numOfGoodLars = (sampleSize * (1.0 - multiplier)).toInt
    val validLarSource = newLarSource(testLars, numOfGoodLars, validLar(_), invalidLar(_))
    validLarSource.mustPass
  }

  property(s"be invalid if single family and withdrawn > $multiplier * total") {
    val numOfGoodLars = (sampleSize * (1.0 - multiplier)).toInt - 1
    val invalidLarSource = newLarSource(testLars, numOfGoodLars, validLar(_), invalidLar(_))
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q047
}
