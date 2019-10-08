package hmda.model.filing.ts

object TsFieldMapping {
  def mapping(ts: TransmittalSheet): Map[String, String] = Map(
    "Record Identifier"                               -> ts.id.toString,
    "Financial Institution Name"                      -> ts.institutionName,
    "Calendar Year"                                   -> ts.year.toString,
    "Calendar Quarter"                                -> ts.quarter.toString,
    "Contact Person's Name"                           -> ts.contact.name,
    "Contact Person's Phone Number"                   -> ts.contact.phone,
    "Contact Person's E-mail Address"                 -> ts.contact.email,
    "Contact Person's Office Street Address"          -> ts.contact.address.street,
    "Contact Person's Office City"                    -> ts.contact.address.city,
    "Contact Person's Office State"                   -> ts.contact.address.state,
    "Contact Person's Office Zip Code"                -> ts.contact.address.zipCode,
    "Federal Agency"                                  -> ts.agency.code.toString,
    "Total Number of Entries Contained in Submission" -> ts.totalLines.toString,
    "Federal Taxpayer Identification Number"          -> ts.taxId,
    "Legal Entity Identifier (LEI)"                   -> ts.LEI,
    "All data fields in the LAR"                      -> "Your LAR contains one or more duplicate records."
  )
}
