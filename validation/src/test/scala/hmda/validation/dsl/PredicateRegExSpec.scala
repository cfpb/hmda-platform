package hmda.validation.dsl

import hmda.parser.fi.ts.TsGenerators
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateSyntax._
import org.scalacheck.Gen

class PredicateRegExSpec
    extends PropSpec
    with PropertyChecks
    with TsGenerators {

  // Email **************

  property("A valid email must pass the email regex") {
    forAll(emailGen) { email =>
      email is validEmail
    }
  }

  property("An alphanumeric string will fail the email regex") {
    forAll(Gen.alphaStr) { email =>
      email not validEmail
    }
  }

  property("An empty string will fail the email regex") {
    "" not validEmail
  }

  property("An improperly formatted email must fail the email regex") {
    val testCases = List("test@.", "test@test.", "test@.com", "@test.com", "@.", " @ . ", "!test@test.com",
      "test@test.com!", "123@456.789")
    testCases.foreach(_ not validEmail)
  }

  // Phone **************

  property("A valid phone number must pass the phone regex") {
    forAll(phoneGen) { phone =>
      phone is validPhoneNumber
    }
  }

  property("A numeric string will fail the phone regex") {
    forAll(Gen.numStr) { phone =>
      phone not validPhoneNumber
    }
  }

  property("An empty string will fail the phone regex") {
    "" not validPhoneNumber
  }

  property("An improperly formatted phone number must fail the phone number regex") {
    val testCases = List("123-456-789", "--", " - - ", "0-0-0", "abc-def-ghij", "(123) 456-7890", "123 456 7890",
      "1-800-123-4567")
    testCases.foreach(_ not validPhoneNumber)
  }

  // Zip code **************

  property("A valid zip code must pass the zip code regex") {
    forAll(zipGen) { zip =>
      zip is validZipCode
    }
  }

  property("A numeric string of length != 5 will fail the zip code regex") {
    forAll(Gen.numStr.filter(_.length != 5)) { zip =>
      zip not validZipCode
    }
  }

  property("An empty string will fail the zip code regex") {
    "" not validZipCode
  }

  property("An improperly formatted zip code must fail the zip code regex") {
    val testCases = List("1234", " ", "     ", "0-0", "1234-5678", "123456-789", "abcde")
    testCases.foreach(_ not validZipCode)
  }

  // Tax ID **************

  property("A valid tax ID must pass the tax ID regex") {
    forAll(taxIdGen) { taxId =>
      taxId is validTaxId
    }
  }

  property("A numeric string must fail the tax ID regex") {
    forAll(Gen.numStr) { taxId =>
      taxId not validTaxId
    }
  }

  property("An empty string must fail the tax ID regex") {
    "" not validTaxId
  }

  property("An improperly formatted tax ID must fail the tax ID regex") {
    val testCases = List(" - ", "-", "123-456789", "ab-defjhij")
    testCases.foreach(_ not validTaxId)
  }
}
