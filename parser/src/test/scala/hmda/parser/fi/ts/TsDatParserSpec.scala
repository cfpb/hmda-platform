package hmda.parser.fi.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.util.FITestData
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class TsDatParserSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  import FITestData._

  val tsData = TsDatParser(tsDAT)

  property("parse transmittal sheet respondent info") {
    val respondent = tsData.respondent
    respondent.id mustBe "11013201"
    respondent.name mustBe "SMALL BANK USA, NA"
    respondent.address mustBe "1850 TYSONS BLVD., SUITE 50"
    respondent.city mustBe "MCLEAN"
    respondent.state mustBe "VA"
    respondent.zipCode mustBe "22102"
  }

  property("generate proper dat format") {
    forAll(tsGen) { (ts: TransmittalSheet) =>
      val parsedTs = TsDatParser(ts.toDAT)
      val updatedRespondent = parsedTs.respondent.copy(id = parsedTs.respondent.id.replaceFirst("^0+(?!$)", ""))
      val updatedTs = parsedTs.copy(respondent = updatedRespondent)
      updatedTs mustBe ts
    }
  }

  property("parse other info about the respondent") {
    tsData.taxId mustBe "20-1177984"
  }

  property("parse transmittal info") {
    tsData.agencyCode mustBe 9
    tsData.timestamp mustBe 201501171330L
    tsData.activityYear mustBe 2013
    tsData.totalLines mustBe 551
  }

  property("parse parent info") {
    val parent = tsData.parent
    parent.name mustBe "BIGS USA, INC."
    parent.address mustBe "412 THIRD AVENUE"
    parent.city mustBe "NEW YORK"
    parent.zipCode mustBe "10012"
  }

  property("parse contact info") {
    val contact = tsData.contact
    contact.name mustBe "Bob Smith"
    contact.phone mustBe "555-555-5555"
    contact.fax mustBe "999-999-9999"
    contact.email mustBe "bob.smith@bank.com"
  }

  property("parse ok when there is no parent info") {
    val tsDataNoParent = TsDatParser(tsDATNoParent)
    val parent = tsDataNoParent.parent
    parent.name mustBe ""
    parent.address mustBe ""
    parent.city mustBe ""
    parent.zipCode mustBe ""
    // other stuff still parses ok
    tsDataNoParent.respondent.name mustBe "SMALL BANK USA, NA"
  }
}
