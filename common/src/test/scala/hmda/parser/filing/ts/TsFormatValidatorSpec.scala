package hmda.parser.filing.ts

import cats.data.NonEmptyList
import cats.data.Validated.{ Invalid, Valid }
import com.typesafe.config.ConfigFactory
import hmda.model.filing.ts.TsGenerators._
import hmda.parser.filing.ts.TsFormatValidator._
import hmda.parser.filing.ts.TsParserErrorModel._
import hmda.parser.filing.ts.TsValidationUtils._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import hmda.parser.filing.ts.TsParserErrorModel.IncorrectNumberOfFieldsTs
import org.scalatest.{ MustMatchers, PropSpec }

class TsFormatValidatorSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val config = ConfigFactory.load()

  val currentYear    = config.getString("hmda.filing.current")
  val numberOfFields = config.getInt(s"hmda.filing.$currentYear.ts.length")

  property("Transmittal Sheet must be valid") {
    forAll(tsGen) { ts =>
      val values = extractValues(ts)
      validateTs(values) mustBe Valid(ts)
    }
  }

  property("Transmittal Sheet must have the correct number of fields") {
    val values = List("a", "b", "c")
    validateTs(values) mustBe Invalid(NonEmptyList.of(IncorrectNumberOfFieldsTs(values.length.toString)))
  }

  property("Transmittal Sheet must report InvalidId for non numeric id field value") {
    forAll(tsGen) { ts =>
      val badId     = badValue()
      val badValues = extractValues(ts).updated(0, badId)
      validateTs(badValues) mustBe Invalid(NonEmptyList.of(InvalidTsId(badId)))
    }
  }

  property("Transmittal Sheet must report InvalidYear for non numeric year field value") {
    forAll(tsGen) { ts =>
      val badYear   = badValue()
      val badValues = extractValues(ts).updated(2, badYear)
      validateTs(badValues) mustBe Invalid(NonEmptyList.of(InvalidYear(badYear)))
    }
  }

  property("Transmittal Sheet must report InvalidQuarter for non numeric quarter field value") {
    forAll(tsGen) { ts =>
      val badQuarter = badValue()
      val badValues  = extractValues(ts).updated(3, badQuarter)
      validateTs(badValues) mustBe Invalid(NonEmptyList.of(InvalidQuarter(badQuarter)))
    }
  }

  property("Transmittal Sheet must report InvalidTotalLines for non numeric total lines field value") {
    forAll(tsGen) { ts =>
      val badTotalLines = badValue()
      val badValues     = extractValues(ts).updated(12, badTotalLines)
      validateTs(badValues) mustBe Invalid(NonEmptyList.of(InvalidTotalLines(badTotalLines)))
    }
  }

  property("Transmittal Sheet must report InvalidAgencyCode for non numeric agency code field value") {
    forAll(tsGen) { ts =>
      val badAgencyCode = badValue()
      val badValues     = extractValues(ts).updated(11, badAgencyCode)
      validateTs(badValues) mustBe Invalid(NonEmptyList.of(InvalidAgencyCode(badAgencyCode)))
    }
  }

  property("Transmittal Sheet must accumulate parsing errors") {
    forAll(tsGen) { ts =>
      val badId     = badValue()
      val badYear   = badValue()
      val badValues = extractValues(ts).updated(0, badId).updated(2, badYear)
      validateTs(badValues) mustBe Invalid(NonEmptyList.of(InvalidTsId(badId), InvalidYear(badYear)))
    }
  }

}