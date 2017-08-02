package hmda.validation.rules.lar

import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

abstract class LarEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  implicit val generatorDriverConfig =
    PropertyCheckConfiguration(minSuccessful = 100, maxDiscardedFactor = 5.0)

  def check: EditCheck[LoanApplicationRegister]

  // TODO consider trying Matcher instead of these methods
  implicit class LarChecker(lar: LoanApplicationRegister) {
    def mustFail = check(lar) mustBe a[Failure]
    def mustPass = check(lar) mustBe a[Success]
  }
}
