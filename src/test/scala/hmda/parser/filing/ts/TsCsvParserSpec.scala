package hmda.parser.filing.ts

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.ts.TsGenerators._
import TsValidationUtils._
import hmda.parser.filing.ts.TsParserErrorModel.{InvalidAgencyCode, InvalidId}

class TsCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Transmittal Sheet CSV Parser must parse values into TS") {
    forAll(tsGen) { ts =>
      val csv = ts.toCSV
      TsCsvParser(csv) mustBe Right(ts)
    }
  }

  property(
    "Transmittal Seet CSV Parser must report parsing errors for invalid TS") {
    forAll(tsGen) { ts =>
      val badId = badValue()
      val badAgencyCode = badValue()
      val badValues =
        extractValues(ts).updated(0, badId).updated(11, badAgencyCode)
      val csv = badValues.mkString("|")
      TsCsvParser(csv) mustBe Left(List(InvalidId, InvalidAgencyCode))
    }
  }
}
