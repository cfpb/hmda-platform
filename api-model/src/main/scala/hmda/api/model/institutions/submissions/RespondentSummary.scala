package hmda.api.model.institutions.submissions

case class RespondentSummary(
  name: String,
  id: String,
  taxId: String,
  agency: String,
  contact: ContactSummary
)
