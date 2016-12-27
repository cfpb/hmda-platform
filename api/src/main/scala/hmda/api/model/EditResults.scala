package hmda.api.model

import hmda.validation.engine.MacroEditJustification

case class RowId(rowId: String)
case class editResultRow(row: RowId)
case class EditResult(edit: String, description: String, rows: Seq[editResultRow])
case class EditResults(edits: Seq[EditResult])
case object EditResults {
  def empty: EditResults = EditResults(Nil)
}
case class MacroResult(edit: String, justifications: Set[MacroEditJustification])
case class MacroResults(edits: Seq[MacroResult])
case object MacroResults {
  def empty: MacroResults = MacroResults(Nil)
}
case class SummaryEditResults(syntactical: EditResults, validity: EditResults, quality: EditResults, `macro`: MacroResults)

