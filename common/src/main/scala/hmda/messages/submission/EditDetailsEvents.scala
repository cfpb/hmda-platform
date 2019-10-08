package hmda.messages.submission

import hmda.model.edits.EditDetails

object EditDetailsEvents {
  trait EditDetailsPersistenceEvent

  case class EditDetailsAdded(editDetails: EditDetails) extends EditDetailsPersistenceEvent

  case class EditDetailsRowCounted(count: Int) extends EditDetailsPersistenceEvent
}
