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
  def irrelevantLar(setDollarAmount: LoanApplicationRegister => LoanApplicationRegister)(lar: LoanApplicationRegister) = {
    val irrelevantLoan = lar.loan.copy(propertyType = 4)
    val intermediateLar = lar.copy(loan = irrelevantLoan)
    setDollarAmount(intermediateLar)
  }
  def relevantLar(setDollarAmount: LoanApplicationRegister => LoanApplicationRegister)(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(propertyType = 3)
    val intermediate = lar.copy(loan = relevantLoan)
    setDollarAmount(intermediate)
  }
  def setLoanAmount(lar: LoanApplicationRegister, int: Int) = {
    val relevantLoan = lar.loan.copy(amount = int)
    lar.copy(loan = relevantLoan)
  }

  property(s"be valid if multifamily < $larsMultiplier * total and dollar amount multifamily < $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt - 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier - .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustPass
  }

  property(s"be valid if multifamily < $larsMultiplier * total and dollar amount multifamily = $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt - 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustPass
  }

  property(s"be valid if multifamily < $larsMultiplier * total and dollar amount multifamily > $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt - 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier + .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustPass
  }

  property(s"be valid if multifamily = $larsMultiplier * total and dollar amount multifamily < $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier - .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustPass
  }

  property(s"be invalid if multifamily = $larsMultiplier * total and dollar amount multifamily = $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustFail
  }

  property(s"be invalid if multifamily = $larsMultiplier * total and dollar amount multifamily > $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier + .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustFail
  }

  property(s"be valid if multifamily > $larsMultiplier * total and dollar amount multifamily < $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt + 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier - .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustPass
  }

  property(s"be invalid if multifamily > $larsMultiplier * total and dollar amount multifamily = $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt + 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustFail
  }

  property(s"be invalid if multifamily > $larsMultiplier * total and dollar amount multifamily > $larsAmountMultiplier") {
    val numOfRelevantLars = (sampleSize * larsMultiplier).toInt + 1
    val settingRelevantAmount = setRelevantAmount(100, numOfRelevantLars, sampleSize, setLoanAmount, larsAmountMultiplier + .01)(_)
    val settingIrrelevantAmount = setIrrelevantAmount(100, setLoanAmount)(_)
    val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar(settingRelevantAmount), irrelevantLar(settingIrrelevantAmount))
    validLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q015
}
