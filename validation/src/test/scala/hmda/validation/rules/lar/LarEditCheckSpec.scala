package hmda.validation.rules.lar

import hmda.parser.fi.lar.LarGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

abstract class LarEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

}
