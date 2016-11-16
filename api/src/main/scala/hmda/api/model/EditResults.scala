package hmda.api.model

case class LarId(loanId: String)
case class LarEditResult(lar: LarId)
case class EditResult(edit: String, ts: Boolean, lars: Seq[LarEditResult])
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

