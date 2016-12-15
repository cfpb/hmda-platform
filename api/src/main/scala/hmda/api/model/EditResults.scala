package hmda.api.model

import hmda.model.fi.RecordField

case class LarEditField(name: String, value: String)
case class LarEditResult(larId: String, fields: List[LarEditField])
case class EditResult(edit: String, description: String, fields: List[RecordField], lars: Seq[LarEditResult])
case class EditResults(edits: Seq[EditResult])
case object EditResults {
  def empty: EditResults = EditResults(Nil)
}
case class Justification(value: String, selected: Boolean)
case class MacroResult(edit: String, justifications: Seq[Justification])
case class MacroResults(edits: Seq[MacroResult])
case object MacroResults {
  def empty: MacroResults = MacroResults(Nil)
}
case class SummaryEditResults(syntactical: EditResults, validity: EditResults, quality: EditResults, `macro`: MacroResults)

