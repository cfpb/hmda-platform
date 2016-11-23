package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister

trait lessThanOrEqualToPropertyMacroSpec extends MacroSpec {

  def multiplier: Double

  def relevantLar(lar: LoanApplicationRegister): LoanApplicationRegister
  def irrelevantLar(lar: LoanApplicationRegister): LoanApplicationRegister

  protected def lessThanOrEqualToPropertyTests(name: String, multiplier: Double, relevantLar: LoanApplicationRegister => LoanApplicationRegister, irrelevantLar: LoanApplicationRegister => LoanApplicationRegister): Unit = {
    val testLars = lar100ListGen.sample.getOrElse(Nil)
    val sampleSize = testLars.length
    val targetSize = (sampleSize * multiplier).toInt

    property(s"be valid if $name < $multiplier * total") {
      val numOfRelevantLars = targetSize - 1
      val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }

    property(s"be valid if $name = $multiplier * total") {
      val numOfRelevantLars = targetSize
      val validLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }

    property(s"be invalid if $name > $multiplier * total") {
      val numOfRelevantLars = targetSize + 1
      val invalidLarSource = newLarSource(testLars, numOfRelevantLars, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

}
