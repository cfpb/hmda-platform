package hmda.query.serialization

import hmda.model.institution.Institution
import hmda.persistence.model.serialization.InstitutionViewEvents._
import hmda.persistence.serialization.institutions.InstitutionProtobufConverter._
import hmda.query.view.institutions.InstitutionView.InstitutionViewState

object InstitutionViewProtobufConverter {

  def institutionViewStateToProtobuf(state: InstitutionViewState): InstitutionViewStateMessage = {
    InstitutionViewStateMessage(
      institutions = state.institutions.map(i => institutionToProtobuf(i)).toSeq,
      seqNr = state.seqNr
    )
  }

  def institutionViewStateFromProtobuf(msg: InstitutionViewStateMessage): InstitutionViewState = {
    InstitutionViewState(
      institutions = msg.institutions.map(i => institutionFromProtobuf(i)).toSet[Institution],
      seqNr = msg.seqNr
    )
  }
}
