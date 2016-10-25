package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q008Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q008.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size

  property(s"be valid if withdrawn < $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt - 1
    val newLarSource = newActionTakenTypeSource(testLars, numOfGoodLars, 4, 2)
    newLarSource.mustPass
  }

  property(s"be valid if withdrawn = $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt
    val newLarSource = newActionTakenTypeSource(testLars, numOfGoodLars, 4, 2)
    newLarSource.mustPass
  }

  property(s"be invalid if withdrawn > $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt + 1
    val newLarSource = newActionTakenTypeSource(testLars, numOfGoodLars, 4, 2)
    newLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q008
}
