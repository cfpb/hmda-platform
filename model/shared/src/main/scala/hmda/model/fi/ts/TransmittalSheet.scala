package hmda.model.fi.ts

import hmda.model.fi.{ HasControlNumber, HmdaFileRow, StringPaddingUtils }

case class TransmittalSheet(
    id: Int,
    agencyCode: Int,
    timestamp: Long,
    activityYear: Int,
    taxId: String,
    totalLines: Int,
    respondent: Respondent,
    parent: Parent,
    contact: Contact
) extends HasControlNumber with HmdaFileRow with StringPaddingUtils {

  override def valueOf(field: String): Any = {
    TsFieldMapping.mapping(this)(field)
  }

  def toCSV: String = {
    s"$id|${respondent.id}|$agencyCode|$timestamp|$activityYear" +
      s"|$taxId|$totalLines|${respondent.name}|${respondent.address}" +
      s"|${respondent.city}|${respondent.state}|${respondent.zipCode}" +
      s"|${parent.name}|${parent.address}|${parent.city}|${parent.state}" +
      s"|${parent.zipCode}|${contact.name}|${contact.phone}|${contact.fax}|${contact.email}"

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

