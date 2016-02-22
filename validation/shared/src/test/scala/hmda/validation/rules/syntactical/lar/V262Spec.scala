package hmda.validation.rules.syntactical.lar

import hmda.parser.fi.lar.LarGenerators
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks

class V262Spec extends PropSpec with PropertyChecks with LarGenerators {
  property("If date application received = NA, then action taken type must = 6")(pending)
}
