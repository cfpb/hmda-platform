package hmda.parser.fi.ts

import hmda.model.fi.ts.TsGenerators
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class TsCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("Transmittal Sheet must be parsed from CSV") {
    forAll(tsGen) { (ts) =>
      TsCsvParser(ts.toCSV) mustBe Right(ts)
    }
  }

  property("Must return length error on too long csv") {
    forAll(tsGen) { (ts) =>
      TsCsvParser(ts.toCSV + "|0|1") mustBe Left(List("An incorrect number of data fields were reported: 23 data fields were found, when 21 data fields were expected."))
    }
  }

  property("Must recognize blank fields at the end of ts") {
    forAll(tsGen) { (ts) =>
      TsCsvParser(ts.toCSV + "|") mustBe Left(List("An incorrect number of data fields were reported: 22 data fields were found, when 21 data fields were expected."))
    }
  }

  val validTs = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val unparsableTsOneField = "INVALID|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val unparsableTsTwoFields = "INVALID|0123456789|INVALID|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val unparsableTsTwoFieldsWrongLength = "INVALID|0123456789|9|INVALID|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX|0"

  property("Must return name of non-integer field") {
    TsCsvParser(unparsableTsOneField) mustBe Left(List("Record Identifier is not an integer"))
  }

  property("Must return names of non-integer fields") {
    TsCsvParser(unparsableTsTwoFields) mustBe Left(List("Record Identifier is not an integer", "Agency Code is not an integer"))
  }

  property("Must return only length error on too long csv") {
    TsCsvParser(unparsableTsTwoFieldsWrongLength) mustBe Left(List("An incorrect number of data fields were reported: 22 data fields were found, when 21 data fields were expected."))
  }

}
