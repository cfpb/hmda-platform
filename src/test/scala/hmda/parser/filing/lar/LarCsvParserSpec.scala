package hmda.parser.filing.lar

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.lar.LarGenerators._

class LarCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Loan Application Register CSV Parser must parse values into LAR") {
    forAll(larGen) { lar =>
      val csv = lar.toCSV
      LarCsvParser(csv) mustBe Right(lar)
    }
  }
}
