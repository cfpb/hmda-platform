package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q015Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val larsMultiplier = config.getDouble("hmda.validation.macro.Q015.numOfLarsMultiplier")
  val larsAmountMultiplier = config.getDouble("hmda.validation.macro.Q015.dollarAmountOfLarsMultiplier")

  val testLars = lar100ListGen.sample.getOrElse(Nil)
  val sampleSize = testLars.size
  def irrelevantLar(lar: LoanApplicationRegister) = {
    val irrelevantLoan = lar.loan.copy(propertyType = 4)
    lar.copy(loan = irrelevantLoan)
  }
  def relevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(propertyType = 3)
    lar.copy(loan = relevantLoan)
  }
  def findAmount(lar: LoanApplicationRegister, int: Int) = {
    val relevantLoan = lar.loan.copy(amount = int)
    lar.copy(loan = relevantLoan)
  }

  property(s"be valid if multifamily < $larsMultiplier * total and dollar amount multifamily < $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt - 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier - .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustPass
  }

  property(s"be valid if multifamily < $larsMultiplier * total and dollar amount multifamily = $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt - 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustPass
  }

  property(s"be valid if multifamily < $larsMultiplier * total and dollar amount multifamily > $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt - 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier + .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustPass
  }

  property(s"be valid if multifamily = $larsMultiplier * total and dollar amount multifamily < $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier - .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustPass
  }

  property(s"be valid if multifamily = $larsMultiplier * total and dollar amount multifamily = $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustFail
  }

  property(s"be valid if multifamily = $larsMultiplier * total and dollar amount multifamily > $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier + .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustFail
  }

  property(s"be valid if multifamily > $larsMultiplier * total and dollar amount multifamily < $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt + 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier - .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustPass
  }

  property(s"be valid if multifamily > $larsMultiplier * total and dollar amount multifamily = $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt + 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustFail
  }

  property(s"be valid if multifamily > $larsMultiplier * total and dollar amount multifamily > $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt + 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, findAmount, larsAmountMultiplier + .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, findAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar, settingRelevantAmount, settingIrrelevantAmount)
    validLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q015
}
