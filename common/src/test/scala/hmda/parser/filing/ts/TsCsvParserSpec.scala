package hmda.parser.filing.ts

import hmda.model.filing.ts.TsGenerators._
import hmda.parser.filing.ts.TsParserErrorModel.{InvalidAgencyCode, InvalidId}
import hmda.parser.filing.ts.TsValidationUtils._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class TsCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Transmittal Sheet CSV Parser must parse values into TS") {
    forAll(tsGen) { ts =>
      val csv = ts.toCSV
      TsCsvParser(csv) mustBe Right(ts)
    }
  }

  property(
    "Transmittal Shet CSV Parser must report parsing errors for invalid TS") {
    forAll(tsGen) { ts =>
      val csv = ts.toCSV + "|"
      TsCsvParser(csv) mustBe Left(List(InvalidId, InvalidAgencyCode))
    }
  }

  property(
    "Transmittal Shet CSV Parser must report parsing errors for | in the end") {
    forAll(tsGen) { ts =>
      val badId = badValue()
      val badAgencyCode = badValue()
      val badValues =
        extractValues(ts).updated(0, badId).updated(11, badAgencyCode)
      val csv = badValues.mkString("|")
      val csvWithPipeInEnd = csv + "|"
      TsCsvParser(csvWithPipeInEnd) mustBe Left(
        List(InvalidId, InvalidAgencyCode))
    }
  }

}
