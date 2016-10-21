package hmda.validation.rules.lar.`macro`
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q008Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q008.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size

  property(s"be valid if withdrawn < $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt - 1
    val newLarSource = newSource(numOfGoodLars)
    newLarSource.mustPass
  }

  property(s"be valid if withdrawn = $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt
    val newLarSource = newSource(numOfGoodLars)
    newLarSource.mustPass
  }

  property(s"be invalid if withdrawn > $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt + 1
    val newLarSource = newSource(numOfGoodLars)
    newLarSource.mustFail
  }

  private def newSource(numOfGoodLars: Int) = {
    val goodLars = testLars.map(lar => lar.copy(actionTakenType = 4)).take(numOfGoodLars)
    val badLars = testLars.map(lar => lar.copy(actionTakenType = 2)).drop(numOfGoodLars)
    val newLars = goodLars ::: badLars
    Source.fromIterator(() => newLars.toIterator)
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q008
}
