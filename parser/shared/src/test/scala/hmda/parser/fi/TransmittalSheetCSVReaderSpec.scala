package hmda.parser.fi

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ PropSpec, MustMatchers }
import hmda.parser.test.TransmittalSheetGenerators

class TransmittalSheetCSVReaderSpec extends PropSpec with PropertyChecks with MustMatchers with TransmittalSheetGenerators {

  property("status must convert to and from CSV") {
    forAll(tsGen) { (ts) =>
      whenever(ts.id == 1) {
        TransmittalSheetCSVReader(ts.toCSV) mustBe ts
      }
    }
  }

}
