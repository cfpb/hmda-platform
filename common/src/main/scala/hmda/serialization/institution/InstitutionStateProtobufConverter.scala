package hmda.serialization.institution

import hmda.persistence.institution.InstitutionState
import hmda.persistence.serialization.institution.InstitutionMessage
import hmda.persistence.serialization.institution.institutionstate.InstitutionStateMessage
import hmda.serialization.filing.FilingProtobufConverter._
import hmda.serialization.institution.InstitutionProtobufConverter._

object InstitutionStateProtobufConverter {

  def institutionStateToProtobuf(institutionState: InstitutionState): InstitutionStateMessage =
    InstitutionStateMessage(
      if (institutionState.institution.isEmpty) None
      else Some(institutionToProtobuf(institutionState.institution.get)),
      institutionState.filings.map(s => filingToProtobuf(s))
    )

  def institutionStateFromProtobuf(institutionStateMessage: InstitutionStateMessage): InstitutionState =
    InstitutionState(
      institution = Some(institutionFromProtobuf(institutionStateMessage.institution.getOrElse(InstitutionMessage()))),
      filings = institutionStateMessage.filings
        .map(s => filingFromProtobuf(s))
        .toList
    )

}

