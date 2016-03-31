package hmda.validation.engine.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarGenerators
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

import scalaz.Success

class LarValidityEngineSpec extends PropSpec with PropertyChecks
    with MustMatchers with LarGenerators with LarValidityEngine {

  property("Engine returns success for valid LAR") {
    //TODO: test using a file of sample LARs, since the generator does not
    //  produce records that pass all edit checks
  }
}
