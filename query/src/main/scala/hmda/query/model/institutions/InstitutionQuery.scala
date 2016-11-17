package hmda.query.model.institutions

case class InstitutionQuery(
  id: String,
  name: String,
  cra: Boolean,
  agency: Int,
  institutionType: String,
  parent: Boolean,
  status: Int,
  filingPeriod: Int
)
