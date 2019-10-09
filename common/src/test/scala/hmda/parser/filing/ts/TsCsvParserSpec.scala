package hmda.parser.filing.ts

import hmda.model.filing.ts.TsGenerators._
import hmda.parser.ParserErrorModel.IncorrectNumberOfFields
import hmda.parser.filing.ts.TsParserErrorModel.{InvalidAgencyCode, InvalidTsId}
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
    "Transmittal Sheet CSV Parser must report parsing errors for | in the end") {
    forAll(tsGen) { ts =>
      val csv = ts.toCSV + "|"
      TsCsvParser(csv) mustBe Left(List(IncorrectNumberOfFields(16)))
    }
  }

  property(
    "Transmittal Sheet CSV Parser must report parsing errors for invalid TS") {
    forAll(tsGen) { ts =>
      val badId = badValue()
      val badAgencyCode = badValue()
      val badValues =
        extractValues(ts).updated(0, badId).updated(11, badAgencyCode)
      val csv = badValues.mkString("|")
      TsCsvParser(csv) mustBe Left(List(InvalidTsId, InvalidAgencyCode))
    }
  }

}
