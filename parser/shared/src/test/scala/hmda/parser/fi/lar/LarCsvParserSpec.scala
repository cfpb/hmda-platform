package hmda.parser.fi.lar

import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class LarCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("Loan Application Register must be parsed from CSV") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      LarCsvParser(lar.toCSV).isRight mustBe true
    }
  }

  property("Must recognize blank fields at the end of lar") {
    forAll(larGen) { (lar) =>
      LarCsvParser(lar.toCSV + "|", 40).left.get mustBe LarParsingError(40, List("Incorrect number of fields. found: 40, expected: 39"))
    }
  }

  property("Must return length error on too long csv") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      LarCsvParser(lar.toCSV + "|0|1").left.get mustBe LarParsingError(0, List("Incorrect number of fields. found: 41, expected: 39"))
    }
  }

  val unparsableLarCsvOneField = "a|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|NA|4|2|2|1|100|3|6|20130119|14454|25|025|0001.00|4|3|5|4|3|2|1|6|||||1|2|NA|0||||NA|2|4"

  property("Must return name of non-integer field") {
    LarCsvParser(unparsableLarCsvOneField).left.get mustBe LarParsingError(0, List("Record Identifier is not an Integer"))
  }

  val unparsableLarCsvTwoFields = "a|0123456789|b|ABCDEFGHIJKLMNOPQRSTUVWXY|NA|4|2|2|1|100|3|6|20130119|14454|25|025|0001.00|4|3|5|4|3|2|1|6|||||1|2|NA|0||||NA|2|4"

  property("Must return names of non-integer fields") {
    LarCsvParser(unparsableLarCsvTwoFields).left.get mustBe LarParsingError(0, List("Record Identifier is not an Integer", "Agency Code is not an Integer"))
  }

  val unparsableLarCsvTwoFieldsTooShort = "a|0123456789|b|ABCDEFGHIJKLMNOPQRSTUVWXY|NA|4|2|2|1|100|3|6|20130119|14454|25|025|0001.00|4|3|5|4|3|2|1|6|||||1|2|NA|0||||NA|2"

  property("Must return only length error on too short csv") {
    LarCsvParser(unparsableLarCsvTwoFieldsTooShort).left.get mustBe LarParsingError(0, List("Incorrect number of fields. found: 38, expected: 39"))
  }
}
