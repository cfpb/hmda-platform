package hmda.model.fi.ts

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
) {

  def toCSV: String = {
    s"$id|${respondent.id}|$agencyCode|$timestamp|$activityYear" +
      s"|$taxId|$totalLines|${respondent.name}|${respondent.address}" +
      s"|${respondent.city}|${respondent.state}|${respondent.zipCode}" +
      s"|${parent.name}|${parent.address}|${parent.city}|${parent.state}" +
      s"|${parent.zipCode}|${contact.name}|${contact.phone}|${contact.fax}|${contact.email}"

  }

}

