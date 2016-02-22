package hmda.validation.rules.syntactical.lar

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S0140Spec extends PropSpec with PropertyChecks with MustMatchers {
  property("Loan/Application number must be unique")(pending)
}
