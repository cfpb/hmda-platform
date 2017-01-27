package hmda.model.fi.ts

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class TransmittalSheetSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("must return value from friendly field name: Tax ID") {
    forAll(tsGen) { (ts: TransmittalSheet) =>
      ts.valueOf("Tax ID") mustBe ts.taxId
    }
  }

  property("must return value from friendly field name: Timestamp") {
    forAll(tsGen) { (ts: TransmittalSheet) =>
      ts.valueOf("Date of Action") mustBe ts.timestamp
    }
  }

  property("must return value from friendly field name: Respondent Address") {
    forAll(tsGen) { (ts: TransmittalSheet) =>
      ts.valueOf("Respondent Address") mustBe ts.respondent.address
    }
  }

  property("must return value from friendly field name: Parent Name") {
    forAll(tsGen) { (ts: TransmittalSheet) =>
      ts.valueOf("Parent Name") mustBe ts.parent.name
    }
  }

  property("must return value from friendly field name: Contact Fax") {
    forAll(tsGen) { (ts: TransmittalSheet) =>
      ts.valueOf("Contact Person's Facsimile Number") mustBe ts.contact.fax
    }
  }
}
