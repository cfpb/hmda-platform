package hmda.validation.rules.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

abstract class MultipleLarEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  implicit val generatorDriverConfig =
    PropertyCheckConfiguration(minSuccessful = 100, maxDiscardedFactor = 5.0)

  def check: EditCheck[Iterable[LoanApplicationRegister]]

  implicit class LarsChecker(lars: Iterable[LoanApplicationRegister]) {
    def mustFail = check(lars) mustBe a[Failure]
    def mustPass = check(lars) mustBe a[Success]
  }
}
