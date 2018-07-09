package hmda.model.fi.ts

import hmda.model.fi.{ HasControlNumber, HmdaFileRow, StringPaddingUtils }

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class TransmittalSheet(
    id: Int = 0,
    agencyCode: Int = 0,
    timestamp: Long = 0L,
    activityYear: Int = 0,
    taxId: String = "",
    totalLines: Int = 0,
    respondent: Respondent = Respondent("", "", "", "", "", ""),
    parent: Parent = Parent("", "", "", "", ""),
    contact: Contact = Contact("", "", "", "")
) extends HasControlNumber with HmdaFileRow with StringPaddingUtils {

  def isEmpty: Boolean = {
    this.id == 0 &&
      this.agencyCode == 0 &&
      this.timestamp == 0L &&
      this.activityYear == 0 &&
      this.taxId == "" &&
      this.totalLines == 0 &&
      this.respondent.id == "" &&
      this.respondent.zipCode == "" &&
      this.respondent.state == "" &&
      this.respondent.city == "" &&
      this.respondent.address == "" &&
      this.respondent.name == "" &&
      this.parent.zipCode == "" &&
      this.parent.state == "" &&
      this.parent.city == "" &&
      this.parent.address == "" &&
      this.parent.name == "" &&
      this.contact.email == "" &&
      this.contact.fax == "" &&
      this.contact.phone == "" &&
      this.contact.name == ""
  }

  override def valueOf(field: String): Any = {
    TsFieldMapping.mapping(this).getOrElse(field, "error: field name mismatch")
  }

  def errorId: String = this.agencyCode + this.respondent.id

  def toCSV: String = {
    s"$id|${respondent.id}|$agencyCode|$timestamp|$activityYear" +
      s"|$taxId|$totalLines|${respondent.name}|${respondent.address}" +
      s"|${respondent.city}|${respondent.state}|${respondent.zipCode}" +
      s"|${parent.name}|${parent.address}|${parent.city}|${parent.state}" +
      s"|${parent.zipCode}|${contact.name}|${contact.phone}|${contact.fax}|${contact.email}"
  }

  def toCSVModified: String = {
    s"$id|${respondent.id}|$agencyCode|$timestamp|$activityYear" +
      s"|$taxId|$totalLines|${respondent.name}|${respondent.address}" +
      s"|${respondent.city}|${respondent.state}|${respondent.zipCode}" +
      s"|${parent.name}|${parent.address}|${parent.city}|${parent.state}" +
      s"|${parent.zipCode}"
  }

  /**
   * NOTE:  The DAT file format is not supported by CFPB
   */
  def toDAT: String = {
    id +
      padLeftWithZero(respondent.id, 10) +
      agencyCode +
      timestamp +
      " " +
      activityYear +
      taxId +
      padRight(totalLines.toString, 7) +
      padRight(respondent.name, 30) +
      padRight(respondent.address, 40) +
      padRight(respondent.city, 25) +
      respondent.state +
      padRight(respondent.zipCode, 10) +
      padRight(parent.name, 30) +
      padRight(parent.address, 40) +
      padRight(parent.city, 25) +
      parent.state +
      padRight(parent.zipCode, 10) +
      padRight(contact.name, 30) +
      contact.phone +
      contact.fax +
      padRight(contact.email, 66)
  }

  override def respondentId: String = respondent.id

}
