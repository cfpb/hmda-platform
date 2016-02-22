package hmda.validation.rules.validity.lar

import hmda.parser.fi.lar.LarGenerators
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks

//TODO: implement when https://github.com/cfpb/hmda-platform/issues/71 is merged
class V220Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("Loan Type must = 1,2,3, or 4")(pending)
}
