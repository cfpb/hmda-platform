package hmda.parser.institution

import hmda.model.institution.InstitutionGenerators._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks

class InstitutionCsvParserSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Institution CSV Parser must parse values into CSV") {
    forAll(institutionGen) { institution =>
      val csv = institution.toCSV
      InstitutionCsvParser(csv) mustBe institution
    }
  }

}
