package hmda.validation.dsl

import hmda.parser.fi.lar.LarGenerators
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.engine.lar.syntactical.LarSyntacticalEngine
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import org.scalacheck.Gen

class PredicateRegExSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarSyntacticalEngine
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

  property("An empty string will fail the regex") {
    "" not validEmail
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

}
