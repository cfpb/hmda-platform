package hmda.parser.apor

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.apor.APORGenerator._

class APORCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("APOR must be parsed from CSV") {
    forAll(APORGen) { apor =>
      APORCsvParser(apor.toCSV) mustBe apor
    }
  }
}
