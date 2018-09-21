package hmda.validation.dsl

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.validation.dsl.PredicateRegEx._
import hmda.generators.CommonGenerators._
import hmda.model.filing.ts.TsGenerators._
import org.scalacheck.Gen

class PredicateRegexSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("All generated emails must pass the email regex") {
    forAll(emailGen) { email =>
      validEmail.check(email) mustBe true
    }
  }

  property("All valid emails must pass the email regex") {
    val testCases = List(
      "what.e.v.e.r@example.co.uk",
      "6237468@example.gov",
      "f@a.ke",
      "lol@12345.lol",
      "hi__mom@example.club",
      "this+that@there.then",
      "_@here.or",
      "-@there.and",
      "+@12.three",
      "a@-.bc"
    )
    testCases.foreach(validEmail.check(_) mustBe true)
  }

  property("An alphanumeric string will fail the email regex") {
    forAll(Gen.alphaStr) { email =>
      validEmail.check(email) mustBe false
    }
  }

  property("An empty string will fail the email regex") {
    validEmail.check("") mustBe false
  }

  property("An improperly formatted email must fail the email regex") {
    val emails = List("test@.",
                      "test@test.",
                      "test@.com",
                      "@test.com",
                      "@.",
                      " @ . ",
                      "!test@test.com",
                      "test@test.com!",
                      "123@456.789")
    emails.foreach(validEmail.check(_) mustBe false)
  }

  property("A valid phone number must pass the phone regex") {
    forAll(phoneGen) { phone =>
      validPhoneNumber.check(phone) mustBe true
    }
  }

  property("A valid tax id must pass the tax id regex") {
    forAll(taxIdGen) { taxId =>
      validTaxId.check(taxId) mustBe true
    }
  }

  property("An invalid tax id must fail the tax id regex") {
    val testCases = List(
      "11-111111a",
      "123456789",
      "1-23456789",
      "12--3456789",
      "ab-cdefghi",
      "",
      "-",
      "2-7",
      "12-cdefghi",
      "12.3456789"
    )
    testCases.foreach(validTaxId.check(_) mustBe false)
  }

  property("A numeric string will fail the phone regex") {
    forAll(Gen.numStr) { phone =>
      validPhoneNumber.check(phone) mustBe false
    }
  }

  property("An empty string will fail the phone regex") {
    validPhoneNumber.check("") mustBe false
  }

  property(
    "An improperly formatted phone number must fail the phone number regex") {
    val phoneNumbers = List("123-456-789",
                            "--",
                            " - - ",
                            "0-0-0",
                            "abc-def-ghij",
                            "(123) 456-7890",
                            "123 456 7890",
                            "1-800-123-4567")
    phoneNumbers.foreach(validPhoneNumber.check(_) mustBe false)
  }

  property("A valid zip code must pass the zip code regex") {
    forAll(zipGen) { zip =>
      validZipCode.check(zip) mustBe true
    }
  }

  property("A numeric string of length != 5 will fail the zip code regex") {
    forAll(Gen.numStr.filter(_.length != 5)) { zip =>
      validZipCode.check(zip) mustBe false
    }
  }

  property("An empty string will fail the zip code regex") {
    validZipCode.check("") mustBe false
  }

  property("An improperly formatted zip code must fail the zip code regex") {
    val testCases =
      List("1234", " ", "     ", "0-0", "1234-5678", "123456-789", "abcde")
    testCases.foreach(validZipCode.check(_) mustBe false)
  }

  property("A non-alphanumeric string will fail the regex") {
    val testCases =
      List("a.b", "a+b", "a/b", "a-b", ".", "@", "abcdefg93809asdf-a")
    testCases.foreach(alphanumeric.check(_) mustBe false)
  }

}
