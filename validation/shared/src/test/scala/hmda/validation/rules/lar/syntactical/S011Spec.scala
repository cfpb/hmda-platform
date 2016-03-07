package hmda.validation.rules.lar.syntactical

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class S011Spec extends PropSpec with PropertyChecks with MustMatchers {

  //TODO: implement once https://github.com/cfpb/hmda-platform/issues/71 is merged
  property("LAR list must not be empty")(pending)
}
