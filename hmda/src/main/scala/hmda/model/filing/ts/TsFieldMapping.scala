package hmda.model.filing.ts

object TsFieldMapping {
  def mapping(ts: TransmittalSheet): Map[String, Any] = Map(
    "Record Identifier" -> ts.id,
    "Financial Institution Name" -> ts.institutionName,
    "Calendar Year" -> ts.year,
    "Calendar Quarter" -> ts.quarter,
    "Contact Person's Name" -> ts.contact.name,
    "Contact Person's Phone Number" -> ts.contact.phone,
    "Contact Person's E-mail Address" -> ts.contact.email,
    "Contact Person's Office Street Address" -> ts.contact.address.street,
    "Contact Person's Office City" -> ts.contact.address.city,
    "Contact Person's Office State" -> ts.contact.address.state,
    "Contact Person's Office Zip Code" -> ts.contact.address.zipCode,
    "Federal Agency" -> ts.agency.code,
    "Total Line Entries" -> ts.totalLines,
    "Tax ID" -> ts.taxId,
    "Legal Entity Identifier" -> ts.LEI
  )
}
