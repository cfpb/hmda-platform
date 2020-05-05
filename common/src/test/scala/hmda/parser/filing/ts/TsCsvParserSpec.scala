package hmda.parser.filing.ts

import hmda.model.filing.ts.TsGenerators._
import hmda.parser.filing.ts.TsParserErrorModel.{ InvalidAgencyCode, InvalidTsId }
import hmda.parser.filing.ts.TsValidationUtils._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class TsCsvParserSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Transmittal Sheet CSV Parser must parse values into TS") {
    forAll(tsGen) { ts =>
      val csv = ts.toCSV
      TsCsvParser(csv) mustBe Right(ts)
    }
  }

  property("Transmittal Sheet CSV Parser must remove | from end of row and  and control characters") {
    forAll(tsGen) { ts =>
      val csv = ts.toCSV + "|\r\n"
      TsCsvParser(csv) mustBe Right(ts)
    }
  }

  property("Transmittal Sheet CSV Parser must report parsing errors for invalid TS") {
    forAll(tsGen) { ts =>
      val badId         = badValue()
      val badAgencyCode = badValue()
      val badValues =
        extractValues(ts).updated(0, badId).updated(11, badAgencyCode)
      val csv = badValues.mkString("|")
      TsCsvParser(csv) mustBe Left(List(InvalidTsId(badId), InvalidAgencyCode(badAgencyCode)))
    }
  }

}