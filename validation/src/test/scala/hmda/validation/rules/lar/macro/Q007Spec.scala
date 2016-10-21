package hmda.validation.rules.lar.`macro`

import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q007Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q007.numOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size

  property(s"be valid if not accepted < $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt - 1
    val newLarSource = newSource(numOfGoodLars)
    newLarSource.mustPass
  }

  property(s"be valid if not accepted = $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt
    val newLarSource = newSource(numOfGoodLars)
    newLarSource.mustPass
  }

  property(s"be invalid if not accepted > $multiplier * total") {
    val numOfGoodLars = (sampleSize * multiplier).toInt + 1
    val newLarSource = newSource(numOfGoodLars)
    newLarSource.mustFail
  }

  private def newSource(numOfGoodLars: Int) = {
    val goodLars = testLars.map(lar => lar.copy(actionTakenType = 2)).take(numOfGoodLars)
    val badLars = testLars.map(lar => lar.copy(actionTakenType = 4)).drop(numOfGoodLars)
    val newLars = goodLars ::: badLars
    Source.fromIterator(() => newLars.toIterator)
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q007
}
