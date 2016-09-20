package hmda.api.model

case class LarEditResult(loanId: String)
case class EditResult(edit: String, lars: Seq[LarEditResult])
case class EditResults(edits: Seq[EditResult])

