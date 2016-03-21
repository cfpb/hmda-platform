package hmda.parser.fi.ts

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class TsCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("Transmittal Sheet must be parsed from CSV") {
    forAll(tsGen) { (ts) =>
      whenever(ts.id == 1) {
        TsCsvParser(ts.toCSV) mustBe ts
      }
    }
  }

}
