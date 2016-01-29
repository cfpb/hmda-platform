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
)

