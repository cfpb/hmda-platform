package hmda.parser.fi.ts

import hmda.model.util.FITestData
import org.scalatest._

class TsDatParserSpec extends FlatSpec with MustMatchers {

  import FITestData._

  val tsData = TsDatParser(tsDAT)

  "TS Parser" should "parse transmittal sheet respondent info" in {
    val respondent = tsData.respondent
    respondent.id mustBe "0011013201"
    respondent.name mustBe "SMALL BANK USA, NA"
    respondent.address mustBe "1850 TYSONS BLVD., SUITE 50"
    respondent.city mustBe "MCLEAN"
    respondent.state mustBe "VA"
    respondent.zipCode mustBe "22102"
  }

  it should "parse other info about the respondent" in {
    tsData.taxId mustBe "20-1177984"
  }

  it should "parse transmittal info" in {
    tsData.agencyCode mustBe 9
    tsData.timestamp mustBe 201501171330L
    tsData.activityYear mustBe 2013
    tsData.totalLines mustBe 551
  }

  it should "parse parent info" in {
    val parent = tsData.parent
    parent.name mustBe "BIGS USA, INC."
    parent.address mustBe "412 THIRD AVENUE"
    parent.city mustBe "NEW YORK"
    parent.zipCode mustBe "10012"
  }

  it should "parse contact info" in {
    val contact = tsData.contact
    contact.name mustBe "Bob Smith"
    contact.phone mustBe "555-555-5555"
    contact.fax mustBe "999-999-9999"
    contact.email mustBe "bob.smith@bank.com"
  }

  it should "parse ok when there is no parent info" in {
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
