package hmda.validation.rules.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

abstract class LarEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  implicit val generatorDriverConfig =
    PropertyCheckConfig(minSuccessful = 100, maxDiscarded = 500)

  def check: EditCheck[LoanApplicationRegister]

  // TODO consider trying Matcher instead of these methods
  implicit class LarChecker(lar: LoanApplicationRegister) {
    def mustFail = check(lar) mustBe a[Failure]
    def mustPass = check(lar) mustBe a[Success]
  }
}
