package hmda.validation.dsl

import hmda.parser.fi.ts.TsGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.validation.dsl.PredicateRegEx._
import org.scalacheck.Gen

class PredicateRegExSpec
    extends PropSpec
    with PropertyChecks
    with TsGenerators
    with MustMatchers {

  // Email **************

  property("All generated emails must pass the email regex") {
    forAll(emailGen) { email =>
      validEmail.validate(email) mustBe true
    }
  }

  property("All valid emails must pass the email regex") {
    val testCases = List("what.e.v.e.r@example.co.uk", "6237468@example.gov",
      "f@a.ke", "lol@12345.lol", "hi__mom@example.club", "this+that@there.then",
      "_@here.or", "-@there.and", "+@12.three", "a@-.bc")
    testCases.foreach(validEmail.validate(_) mustBe true)
  }

  property("An alphanumeric string will fail the email regex") {
    forAll(Gen.alphaStr) { email =>
      validEmail.validate(email) mustBe false
    }
  }

  property("An empty string will fail the email regex") {
    validEmail.validate("") mustBe false
  }

  property("An improperly formatted email must fail the email regex") {
    val testCases = List("test@.", "test@test.", "test@.com", "@test.com", "@.", " @ . ", "!test@test.com",
      "test@test.com!", "123@456.789")
    testCases.foreach(validEmail.validate(_) mustBe false)
  }

  // Phone **************

  property("A valid phone number must pass the phone regex") {
    forAll(phoneGen) { phone =>
      validPhoneNumber.validate(phone) mustBe true
    }
  }

  property("A numeric string will fail the phone regex") {
    forAll(Gen.numStr) { phone =>
      validPhoneNumber.validate(phone) mustBe false
    }
  }

  property("An empty string will fail the phone regex") {
    validPhoneNumber.validate("") mustBe false
  }

  property("An improperly formatted phone number must fail the phone number regex") {
    val testCases = List("123-456-789", "--", " - - ", "0-0-0", "abc-def-ghij", "(123) 456-7890", "123 456 7890",
      "1-800-123-4567")
    testCases.foreach(validPhoneNumber.validate(_) mustBe false)
  }

  // Zip code **************

  property("A valid zip code must pass the zip code regex") {
    forAll(zipGen) { zip =>
      validZipCode.validate(zip) mustBe true
    }
  }

  property("A numeric string of length != 5 will fail the zip code regex") {
    forAll(Gen.numStr.filter(_.length != 5)) { zip =>
      validZipCode.validate(zip) mustBe false
    }
  }

  property("An empty string will fail the zip code regex") {
    validZipCode.validate("") mustBe false
  }

  property("An improperly formatted zip code must fail the zip code regex") {
    val testCases = List("1234", " ", "     ", "0-0", "1234-5678", "123456-789", "abcde")
    testCases.foreach(validZipCode.validate(_) mustBe false)
  }

  // Tax ID **************

  property("A valid tax ID must pass the tax ID regex") {
    forAll(taxIdGen) { taxId =>
      validTaxId.validate(taxId) mustBe true
    }
  }

  property("A numeric string must fail the tax ID regex") {
    forAll(Gen.numStr) { taxId =>
      validTaxId.validate(taxId) mustBe false
    }
  }

  property("An empty string must fail the tax ID regex") {
    validTaxId.validate("") mustBe false
  }

  property("An improperly formatted tax ID must fail the tax ID regex") {
    val testCases = List(" - ", "-", "123-456789", "ab-defjhij")
    testCases.foreach(validTaxId.validate(_) mustBe false)
  }
}
