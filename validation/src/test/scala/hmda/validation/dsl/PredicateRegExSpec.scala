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

  // Email

  property("A valid email must pass the regex") {
    forAll(emailGen) { email =>
      email is validEmail
    }
  }

  property("An alphanumeric string will fail the regex") {
    forAll(Gen.alphaStr) { email =>
      email not validEmail
    }
  }

  // Phone

  property("A valid phone number must pass the regex") {
    forAll(phoneGen) { phone =>
      phone is validPhoneNumber
    }
  }

  // Zip code

  property("A valid zip code must pass the regex") {
    forAll(zipGen) { zip =>
      zip is validZipCode
    }
  }

  // Tax ID

  property("A valid tax ID must pass the regex") {
    forAll(taxIdGen) { taxId =>
      taxId is validTaxId
    }
  }

}
