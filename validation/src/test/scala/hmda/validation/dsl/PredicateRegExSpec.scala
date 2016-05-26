package hmda.validation.dsl

import hmda.parser.fi.lar.LarGenerators
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.engine.lar.syntactical.LarSyntacticalEngine
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

class PredicateRegExSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarSyntacticalEngine
    with TsGenerators {

  property("A valid email must pass the regex") {
    forAll(emailGen) { email =>
      email is validEmail
    }
  }
}
