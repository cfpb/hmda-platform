package hmda.parser.fi.lar

import hmda.model.fi.lar.LoanApplicationRegister
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

class LarCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("Loan Application Register must be parsed from CSV") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      LarCsvParser(lar.toCSV) mustBe lar
    }
  }
}
