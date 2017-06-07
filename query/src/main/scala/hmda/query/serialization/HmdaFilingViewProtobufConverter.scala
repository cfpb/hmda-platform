package hmda.query.serialization

import hmda.persistence.model.serialization.HmdaFilingViewState.FilingViewStateMessage
import hmda.query.view.filing.HmdaFilingView.FilingViewState

object HmdaFilingViewProtobufConverter {

  def filingViewStateToProtobuf(state: FilingViewState): FilingViewStateMessage = {
    FilingViewStateMessage(
      size = state.size,
      seqNr = state.seqNr
    )
  }

  def filingViewStateFromProtobuf(msg: FilingViewStateMessage): FilingViewState = {
    FilingViewState(
      size = msg.size,
      seqNr = msg.seqNr
    )
  }
}
