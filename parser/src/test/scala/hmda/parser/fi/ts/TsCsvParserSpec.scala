package hmda.parser.fi.ts

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class TsCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("Transmittal Sheet must be parsed from CSV") {
    forAll(tsGen) { (ts) =>
      whenever(ts.id == 1) {
        TsCsvParser(ts.toCSV).right.get mustBe ts
      }
    }
  }

  property("Must return length error on too long csv") {
    forAll(tsGen) { (ts) =>
      TsCsvParser(ts.toCSV + "|0|1").left.get mustBe List("Incorrect number of fields. found: 23, expected: 21")
    }
  }

  val validTs = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val unparsableTsOneField = "INVALID|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val unparsableTsTwoFields = "INVALID|0123456789|INVALID|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val unparsableTsTwoFieldsWrongLength = "INVALID|0123456789|9|INVALID|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX|0"

  property("Must return name of non-integer field") {
    TsCsvParser(unparsableTsOneField).left.get mustBe List("Record Identifier is not an Integer")
  }

  property("Must return names of non-integer fields") {
    TsCsvParser(unparsableTsTwoFields).left.get mustBe List("Record Identifier is not an Integer", "Agency Code is not an Integer")
  }

  property("Must return only length error on too short csv") {
    TsCsvParser(unparsableTsTwoFieldsWrongLength).left.get mustBe List("Incorrect number of fields. found: 22, expected: 21")
  }

}
