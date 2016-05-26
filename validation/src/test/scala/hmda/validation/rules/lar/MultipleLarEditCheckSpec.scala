package hmda.validation.rules.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarGenerators
import hmda.validation.rules.EditCheck
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

abstract class MultipleLarEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  implicit val generatorDriverConfig =
    PropertyCheckConfig(minSuccessful = 100, maxDiscarded = 500)
}
