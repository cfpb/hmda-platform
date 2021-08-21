package hmda.messages.submission

import hmda.messages.CommonMessages.Event
import hmda.model.edits.EditDetails

object EditDetailsEvents {
  trait EditDetailsPersistenceEvent extends Event

  case class EditDetailsAdded(editDetails: EditDetails) extends EditDetailsPersistenceEvent

  case class EditDetailsRowCounted(count: Int) extends EditDetailsPersistenceEvent
}