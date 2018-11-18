package hmda.messages.submission

import hmda.model.edits.EditDetail

object EditDetailPersistenceEvents {
  trait EditDetailsPersistenceEvent

  case class EditDetailsAdded(editDetails: EditDetail)
      extends EditDetailsPersistenceEvent
}
