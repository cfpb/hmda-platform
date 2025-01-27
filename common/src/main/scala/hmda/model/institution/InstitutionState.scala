package hmda.persistence.institution

import hmda.messages.institution.InstitutionEvents.{ FilingAdded, InstitutionEvent }
import hmda.model.filing.Filing
import hmda.model.institution.Institution

case class InstitutionState(institution: Option[Institution], filings: List[Filing] = Nil) {
  def isEmpty: Boolean = institution.isEmpty

  def update(event: InstitutionEvent): InstitutionState =
    event match {
      case FilingAdded(filing) =>
        if (filings.contains(filing)) {
          this
        } else {
          InstitutionState(this.institution, filing :: filings)
        }
      case _ => this
    }
}
