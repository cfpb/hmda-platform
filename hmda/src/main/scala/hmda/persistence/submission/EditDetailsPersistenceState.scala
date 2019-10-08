package hmda.persistence.submission

import hmda.messages.submission.EditDetailsEvents.{ EditDetailsAdded, EditDetailsPersistenceEvent }

case class EditDetailsPersistenceState(totalErrorMap: Map[String, Int] = Map.empty) {
  def update(evt: EditDetailsPersistenceEvent): EditDetailsPersistenceState =
    evt match {
      case EditDetailsAdded(editDetail) =>
        val editName  = editDetail.edit
        val rowCount  = editDetail.rows.size
        val editCount = totalErrorMap.getOrElse(editName, 0) + rowCount
        EditDetailsPersistenceState(totalErrorMap.updated(editName, editCount))
      case _ => this
    }
}
