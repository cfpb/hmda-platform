package hmda.model.fi.ts

// Maps a field's friendly name to its value in the LAR record
object TsFieldMapping {
  def mapping(ts: TransmittalSheet): Map[String, Any] = Map(
    "Record Identifier" -> ts.id,
    "Respondent-ID" -> ts.respondent.id,
    "Agency Code" -> ts.agencyCode,
    "Date of Action" -> ts.timestamp,
    "Timestamp" -> ts.timestamp,
    "Activity Year" -> ts.activityYear,
    "Tax ID" -> ts.taxId,
    "Total Line Entries" -> ts.totalLines,
    "Respondent Name" -> ts.respondent.name,
    "Respondent Address" -> ts.respondent.address,
    "Respondent City" -> ts.respondent.city,
    "Respondent State" -> ts.respondent.state,
    "Respondent ZIP Code" -> ts.respondent.zipCode,
    "Parent Name" -> ts.parent.name,
    "Parent Address" -> ts.parent.address,
    "Parent City" -> ts.parent.city,
    "Parent State" -> ts.parent.state,
    "Parent ZIP Code" -> ts.parent.zipCode,
    "Contact Person's Name" -> ts.contact.name,
    "Contact Person's Phone Number" -> ts.contact.phone,
    "Contact Person's Facsimile Number" -> ts.contact.fax,
    "Contact Person's E-mail Address" -> ts.contact.email
  )
}
