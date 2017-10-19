package hmda.validation.engine.lar

import org.scalatest.{ MustMatchers, WordSpec }

class ULISpec extends WordSpec with MustMatchers {

  "A ULI Validation" must {
    "Produce valid check digit" in {
      val uli = "10Bx939c5543TqA1144M999143X"
      ULI.checkDigit(uli) mustBe "10Bx939c5543TqA1144M999143X38"
    }
  }
}
