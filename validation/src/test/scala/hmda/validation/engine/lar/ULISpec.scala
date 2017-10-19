package hmda.validation.engine.lar

import org.scalatest.{ MustMatchers, WordSpec }

class ULISpec extends WordSpec with MustMatchers {

  val validULI = "10Bx939c5543TqA1144M999143X"
  val invalidULI = "10Bx939c5543TqA1144M999133X38"

  "A ULI Validation" must {
    "Produce valid check digit" in {
      ULI.checkDigit(validULI) mustBe 38
      ULI.generateULI(validULI) mustBe "10Bx939c5543TqA1144M999143X38"
    }
    "Validate ULI" in {
      ULI.validate(validULI + "38") mustBe true
      ULI.validate(invalidULI) mustBe false
    }
  }
}
