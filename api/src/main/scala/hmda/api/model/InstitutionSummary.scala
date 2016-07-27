package hmda.api.model

import hmda.model.fi.Filing

case class InstitutionSummary(id: String, name: String, filings: Seq[Filing]) {

  def noInstitution: Boolean = {
    (id == "") && (name == "")
  }

  def noFiling: Boolean = {
    filings.isEmpty
  }

}
