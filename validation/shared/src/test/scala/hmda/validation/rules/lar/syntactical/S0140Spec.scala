package hmda.validation.rules.lar.syntactical

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class S0140Spec extends PropSpec with PropertyChecks with MustMatchers {
  property("Loan/Application number must be unique")(pending)
}
