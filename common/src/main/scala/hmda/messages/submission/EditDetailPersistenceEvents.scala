package hmda.messages.submission

import hmda.model.edits.EditDetails

object EditDetailPersistenceEvents {
  trait EditDetailsPersistenceEvent

  case class EditDetailsAdded(editDetails: EditDetails)
      extends EditDetailsPersistenceEvent
}
