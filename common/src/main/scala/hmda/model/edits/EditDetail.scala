package hmda.model.edits

case class EditDetail(edit: String, rows: Seq[EditDetailRow] = Nil)
