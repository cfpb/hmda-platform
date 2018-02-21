package hmda.validation.dsl

import hmda.model.fi.lar.LarGenerators
import hmda.model.fi.ts.TsGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.validation.dsl.PredicateRegEx._
import org.scalacheck.Gen

class PredicateRegExSpec
    extends PropSpec
    with PropertyChecks
    with TsGenerators
    with LarGenerators
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

  // Census tract **************
  property("A valid census tract must pass the regex") {
    forAll(censusTractGen) { censusTract =>
      validCensusTractFormat.validate(censusTract) mustBe true
    }
  }

  property("A numeric string must fail the census tract regex") {
    forAll(Gen.numStr) { censusTract =>
      validCensusTractFormat.validate(censusTract) mustBe false
    }
  }

  property("An improperly formatted census tract must fail the census tract regex") {
    val testCases = List("1234.ab", " 1234.56", "1234-56", "OOOO.O1", "12.3456", "", ".")
    testCases.foreach(validCensusTractFormat.validate(_) mustBe false)
  }

  // Contains Digits (Q666) **************
  property("An alpha string with no digits must fail the regex") {
    forAll(Gen.alphaStr) { str =>
      containsDigits.validate(str) mustBe false
    }
  }

  property("Alpha strings with punctuation and special characters (but no digits) must fail the regex") {
    val testCases = List("first last", "lastName-denied", "lastname(NewYork)", "last.first", "Smith#III")
    testCases.foreach(containsDigits.validate(_) mustBe false)
  }

  property("A string with any digits must pass the regex") {
    val testCases = List("first4 last", "lastName2-denied", "1lastname(NewYork)", "last.first.9", "Smith#3")
    testCases.foreach(containsDigits.validate(_) mustBe true)
  }

  // Looks like SSN (Q666) **************
  property("A string with format NNN-NN-NNNN must pass the regex") {
    val testCases = List("333-22-4444", "123-45-6789", "789-03-5238", "111-11-1111")
    testCases.foreach(ssnFormat.validate(_) mustBe true)
  }

  property("A string with non-SSN formats must fail the regex") {
    val testCases = List("333-22-55555", "1234-56-7890", "123-ab-4567", "xy-555-55-5555")
    testCases.foreach(ssnFormat.validate(_) mustBe false)
  }

  property("A string with different punctuation must fail the regex") {
    val testCases = List("333224444", "123.45.6789", "789 03 5238", "111,11,1111")
    testCases.foreach(ssnFormat.validate(_) mustBe false)
  }
}
