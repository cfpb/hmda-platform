package hmda.parser.institution

import hmda.model.institution.InstitutionGenerators._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class InstitutionCsvParserSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Institution CSV Parser must parse values into CSV") {
    forAll(institutionGen) { institution =>
      val csv = institution.toCSV
      InstitutionCsvParser(csv) must equal(institution)
    }
  }

  property("Empty emails should parse correctly") {
    forAll(institutionGen) { institution =>
      val i   = institution.copy(emailDomains = List())
      val csv = i.toCSV
      InstitutionCsvParser(csv) must equal(i)
    }
  }

  property("Notes with special characters should work") {
    val specialChars = Set('|', '"', '\n')
    forAll(institutionGen.suchThat(_.notes.exists(specialChars.contains))) { institution =>
      val csv = institution.toCSV
      InstitutionCsvParser(csv) must equal(institution)
    }
  }

}