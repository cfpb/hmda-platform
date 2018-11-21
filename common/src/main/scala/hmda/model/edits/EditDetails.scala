package hmda.model.edits

case class EditDetails(edit: String, rows: Seq[EditDetailsRow] = Nil)
