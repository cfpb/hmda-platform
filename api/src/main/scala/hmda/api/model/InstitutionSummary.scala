package hmda.api.model

import hmda.model.fi.Filing

case class InstitutionSummary(id: String, name: String, filings: Seq[Filing])
