package hmda.parser.apor

import hmda.model.apor.APORGenerator._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class APORCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("APOR must be parsed from CSV") {
    forAll(APORGen) { apor =>
      APORCsvParser(apor.toCSV) mustBe apor
    }
  }
}
